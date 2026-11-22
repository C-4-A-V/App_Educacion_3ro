package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.ActividadDao
import com.example.mpazpro.data.local.CursoDao
import com.example.mpazpro.data.local.EstudianteDao
import com.example.mpazpro.data.local.EstudianteEntity
import com.example.mpazpro.security.PasswordHasher
import com.example.mpazpro.validation.RutValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EstudianteRepository(
    private val estudianteDao: EstudianteDao,
    private val cursoDao: CursoDao,
    private val actividadDao: ActividadDao
) {

    suspend fun obtenerCurso(
        cursoId: Long,
        docenteId: Long
    ) = cursoDao.buscarPorIdYDocente(cursoId, docenteId)

    fun observarEstudiantes(
        cursoId: Long,
        docenteId: Long
    ) = estudianteDao.observarPorCurso(cursoId, docenteId)

    fun observarPerfiles() = estudianteDao.observarPerfiles()

    fun observarRetirados(cursoId: Long, docenteId: Long) = estudianteDao.observarRetirados(cursoId, docenteId)

    suspend fun retirarEstudiantes(ids: List<Long>, cursoId: Long, docenteId: Long) {
        require(ids.size <= 900)
        estudianteDao.retirarSeleccion(ids, cursoId, docenteId)
    }

    suspend fun restaurarEstudiante(id: Long, cursoId: Long, docenteId: Long) {
        check(estudianteDao.restaurar(id, cursoId, docenteId) == 1)
    }

    fun observarProgreso(estudianteId: Long) =
        actividadDao.observarProgresoEstudiante(estudianteId)

    suspend fun registrar(
        cursoId: Long,
        docenteId: Long,
        nombre: String,
        fotoRuta: String,
        rut: String,
        pin: String
    ): Long {
        // No basta con conocer el ID del curso para modificarlo.
        check(obtenerCurso(cursoId, docenteId) != null) {
            "Curso no disponible para este docente"
        }

        val nombreLimpio = nombre
            .trim()
            .replace(Regex("\\s+"), " ")

        require(nombreLimpio.length >= 2)

        val rutNormal = requireNotNull(RutValidator.normalizar(rut))
        require(RutValidator.pinValido(pin))
        require(estudianteDao.buscarPorRut(rutNormal) == null) { "RUT ya registrado" }
        val protegido = withContext(Dispatchers.Default) { PasswordHasher.generar(pin) }
        return estudianteDao.insertar(
            EstudianteEntity(
                cursoId = cursoId, nombre = nombreLimpio, fotoRuta = fotoRuta,
                rut = rutNormal, pinHash = protegido.hash, pinSalt = protegido.salt,
                pinIteraciones = protegido.iteraciones
            )
        )
    }

    suspend fun configurarAcceso(estudianteId: Long, cursoId: Long, docenteId: Long, rut: String, pin: String) {
        val rutNormal = requireNotNull(RutValidator.normalizar(rut))
        require(RutValidator.pinValido(pin))
        val existente = estudianteDao.buscarPorRut(rutNormal)
        require(existente == null || existente.id == estudianteId) { "RUT ya registrado" }
        val protegido = withContext(Dispatchers.Default) { PasswordHasher.generar(pin) }
        check(estudianteDao.actualizarAcceso(estudianteId, cursoId, docenteId,
            rutNormal, protegido.hash, protegido.salt, protegido.iteraciones) == 1)
    }

    suspend fun autenticar(rut: String, pin: String): Long? {
        val rutNormal = RutValidator.normalizar(rut) ?: return null
        if (!RutValidator.pinValido(pin)) return null
        val estudiante = estudianteDao.buscarActivoPorRut(rutNormal) ?: return null
        val hash = estudiante.pinHash ?: return null
        val salt = estudiante.pinSalt ?: return null
        val iteraciones = estudiante.pinIteraciones ?: return null
        val ahora = System.currentTimeMillis()
        if (estudiante.bloqueadoHasta > ahora) return null
        val coincide = withContext(Dispatchers.Default) {
            PasswordHasher.verificar(pin, hash, salt, iteraciones)
        }
        if (coincide) {
            estudianteDao.actualizarIntentos(estudiante.id, 0, 0)
            return estudiante.id
        }
        val fallidos = estudiante.intentosFallidos + 1
        estudianteDao.actualizarIntentos(estudiante.id, if (fallidos >= 5) 0 else fallidos,
            if (fallidos >= 5) ahora + 60_000 else 0)
        return null
    }
}
