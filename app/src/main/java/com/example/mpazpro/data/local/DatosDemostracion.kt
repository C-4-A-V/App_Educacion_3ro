package com.example.mpazpro.data.local

import androidx.room.withTransaction
import com.example.mpazpro.security.PasswordHasher
import com.example.mpazpro.validation.RutValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Carga aditiva: nunca reemplaza cuentas, cursos, actividades o respuestas existentes. */
object DatosDemostracion {
    const val CORREO = "milo@gmail.com"
    const val CLAVE_INICIAL = "Alegria2002"
    const val RUT_ESTUDIANTE = "20983458-8"
    private const val PIN_ESTUDIANTE_INICIAL = "1234"

    suspend fun preparar(database: MpazDatabase): Long = withContext(Dispatchers.Default) {
        // El hash se calcula fuera de la transacción y fuera del hilo principal.
        val protegida = if (database.docenteDao().buscarPorCorreo(CORREO) == null) {
            PasswordHasher.generar(CLAVE_INICIAL)
        } else null
        val rutEstudiante = checkNotNull(RutValidator.normalizar(RUT_ESTUDIANTE))
        val pinEstudiante = if (database.estudianteDao().buscarPorRut(rutEstudiante) == null) {
            PasswordHasher.generar(PIN_ESTUDIANTE_INICIAL)
        } else null
        database.withTransaction {
            val docentes = database.docenteDao()
            var docente = docentes.buscarPorCorreo(CORREO)
            if (docente == null) {
                val hash = checkNotNull(protegida)
                docentes.insertar(DocenteEntity(
                    nombre = "Milo", correo = CORREO,
                    claveHash = hash.hash, claveSalt = hash.salt, claveIteraciones = hash.iteraciones
                ))
                docente = checkNotNull(docentes.buscarPorCorreo(CORREO))
            }
            val cursos = database.cursoDao()
            val cursoId = cursos.buscarPrimero(docente.id)?.id ?: cursos.insertar(
                CursoEntity(docenteId = docente.id, nombre = "3.º Básico A")
            )
            check(cursoId > 0)
            // Nunca se modifica un perfil que ya tenga este RUT, incluso si pertenece a otro curso.
            if (database.estudianteDao().buscarPorRut(rutEstudiante) == null) {
                val protegido = checkNotNull(pinEstudiante)
                database.estudianteDao().insertar(EstudianteEntity(
                    cursoId = cursoId, nombre = "Camilo", rut = rutEstudiante,
                    pinHash = protegido.hash, pinSalt = protegido.salt, pinIteraciones = protegido.iteraciones
                ))
            }
            val dao = database.actividadDao()
            BancoActividades.entidades(cursoId).forEach { actividad ->
                if (dao.buscarPredeterminada(cursoId, actividad.asignaturaId, actividad.titulo) == null) {
                    dao.insertar(actividad)
                }
            }
            cursoId
        }
    }
}
