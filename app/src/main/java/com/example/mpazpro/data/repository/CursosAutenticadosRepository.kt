package com.example.mpazpro.data.repository

import androidx.room.withTransaction
import com.example.mpazpro.data.local.CreacionCursoPendienteEntity
import com.example.mpazpro.data.local.CursoEntity
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.local.VinculoCursoAutenticadoEntity
import com.example.mpazpro.data.remote.ActividadesServidorApi
import com.example.mpazpro.data.remote.CursoDocenteRemoto
import com.example.mpazpro.data.remote.PerfilDocenteRemoto
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ResultadoSincronizacionCursos(
    val incorporados: Int,
    val noVinculados: List<CursoDocenteRemoto>,
    val pendientes: Int
)

class ConflictoCursoLocal : IllegalStateException(
    "Hay cursos guardados cuya sección coincide o necesita revisión. Revisa la asociación antes de continuar."
)

/** La consulta de Spring es autoritativa para propiedad; Room conserva la lista visible e historial. */
class CursosAutenticadosRepository(
    private val base: MpazDatabase,
    private val remoto: DocenteRemotoRepository,
    private val catalogo: ActividadesServidorApi
) {
    private data class Cuenta(val catalogo: String, val perfil: PerfilDocenteRemoto, val local: Long)
    private val mutex = Mutex()
    private var cuentaConfirmada: Cuenta? = null

    private suspend fun cuenta(docenteLocalId: Long): Cuenta {
        val perfil = remoto.recuperarSesion()
        val identidad = catalogo.identidadCatalogo()
        check(identidad.persistente) { "El catálogo no es persistente" }
        val id = requireNotNull(identidad.identificador)
        check(UUID.fromString(id).toString() == id)
        check(remoto.recuperarSesion() == perfil) { "La sesión cambió" }
        val vinculo = base.identidadDocenteDao().buscar(id, perfil.id)
        check(vinculo?.docenteLocalId == docenteLocalId) { "El perfil no corresponde a esta cuenta" }
        return Cuenta(id, perfil, docenteLocalId).also { cuentaConfirmada = it }
    }

    /** Solo para la cuenta y catálogo ya comprobados durante esta sesión. */
    suspend fun pendientesLocales(docenteLocalId: Long): List<CreacionCursoPendienteEntity> = mutex.withLock {
        val cuenta = cuentaConfirmada ?: return@withLock emptyList()
        if (cuenta.local != docenteLocalId || remoto.sesion.value != cuenta.perfil) return@withLock emptyList()
        base.cursoAutenticadoDao().pendientes(cuenta.catalogo, cuenta.perfil.id)
    }

    private fun comprobar(cuenta: Cuenta) {
        check(remoto.sesion.value == cuenta.perfil) { "La sesión cambió" }
    }

    private fun validarCursoCreado(curso: CursoDocenteRemoto, seccion: Char) {
        check(curso.id > 0 && PropuestaVinculacionCursos.seccion(curso.nombre) == seccion) {
            "La respuesta del servidor no corresponde al curso solicitado"
        }
    }

    /** Detecta conflicto para detener la incorporación; nunca decide una asociación. */
    private fun colision(nombre: String, cursos: List<CursoEntity>): Boolean {
        val seccion = PropuestaVinculacionCursos.seccion(nombre)
        return cursos.any {
            it.nombre.equals(nombre, ignoreCase = true) ||
                (seccion != null && PropuestaVinculacionCursos.seccion(it.nombre) == seccion)
        }
    }

    suspend fun actualizar(docenteLocalId: Long): ResultadoSincronizacionCursos = mutex.withLock {
        val cuenta = cuenta(docenteLocalId)
        actualizarCuenta(cuenta)
    }

    private suspend fun actualizarCuenta(cuenta: Cuenta): ResultadoSincronizacionCursos {
        val cursos = remoto.cursos()
        comprobar(cuenta)
        return base.withTransaction {
            comprobar(cuenta)
            val dao = base.cursoAutenticadoDao()
            val pendientes = dao.pendientes(cuenta.catalogo, cuenta.perfil.id)
            val sinVinculo = mutableListOf<CursoDocenteRemoto>()
            var incorporados = 0
            for (curso in cursos) {
                check(curso.id > 0 && curso.nombre.isNotBlank())
                if (dao.buscarVinculo(cuenta.catalogo, cuenta.perfil.id, curso.id) != null) continue
                // Ante un envío incierto solo la respuesta idempotente identifica el curso creado.
                if (pendientes.isNotEmpty()) {
                    sinVinculo.add(curso)
                    continue
                }
                val locales = base.cursoDao().listarPorDocente(cuenta.local)
                if (PropuestaVinculacionCursos.seccion(curso.nombre) == null ||
                    locales.any { PropuestaVinculacionCursos.seccion(it.nombre) == null } ||
                    colision(curso.nombre, locales)) {
                    sinVinculo.add(curso)
                    continue
                }
                val id = base.cursoDao().insertar(CursoEntity(docenteId = cuenta.local, nombre = curso.nombre))
                if (id <= 0) {
                    sinVinculo.add(curso)
                    continue
                }
                dao.insertarVinculo(VinculoCursoAutenticadoEntity(cuenta.catalogo, cuenta.perfil.id, curso.id, id))
                incorporados++
            }
            ResultadoSincronizacionCursos(incorporados, sinVinculo, pendientes.size)
        }
    }

    suspend fun localesVinculados(docenteLocalId: Long): Set<Long> = mutex.withLock {
        val cuenta = cuenta(docenteLocalId)
        base.cursoAutenticadoDao().localesVinculados(cuenta.catalogo, cuenta.perfil.id).toSet()
    }

    suspend fun crear(docenteLocalId: Long, seccion: Char): Long = mutex.withLock {
        val cuenta = cuenta(docenteLocalId)
        enviarCreacion(cuenta, seccion, null)
    }

    suspend fun crearDesdeAnterior(docenteLocalId: Long, cursoLocalId: Long, seccion: Char): Long = mutex.withLock {
        val cuenta = cuenta(docenteLocalId)
        enviarCreacion(cuenta, seccion, cursoLocalId)
    }

    private suspend fun enviarCreacion(cuenta: Cuenta, seccion: Char, cursoAnterior: Long?): Long {
        require(seccion in 'A'..'Z')
        val dao = base.cursoAutenticadoDao()
        val pendiente = base.withTransaction {
            comprobar(cuenta)
            if (cursoAnterior != null) {
                requireNotNull(base.cursoDao().buscarPorIdYDocente(cursoAnterior, cuenta.local))
                check(dao.buscarPorLocal(cursoAnterior) == null) { "El curso ya está asociado" }
                val otros = base.cursoDao().listarPorDocente(cuenta.local).filter { it.id != cursoAnterior }
                if (otros.any { PropuestaVinculacionCursos.seccion(it.nombre) == null } ||
                    colision("3.º Básico $seccion", otros)) throw ConflictoCursoLocal()
            }
            val anteriores = dao.pendientes(cuenta.catalogo, cuenta.perfil.id)
            val existente = dao.buscarPendiente(cuenta.catalogo, cuenta.perfil.id, seccion.toString())
            check(anteriores.isEmpty() || existente != null) { "Resuelve primero la creación pendiente" }
            if (existente != null) {
                check(existente.cursoLocalId == cursoAnterior) { "La creación pendiente corresponde a otro curso" }
                existente
            } else {
                val cursos = base.cursoDao().listarPorDocente(cuenta.local)
                if (cursoAnterior == null &&
                    (cursos.any { PropuestaVinculacionCursos.seccion(it.nombre) == null } ||
                     colision("3.º Básico $seccion", cursos))) throw ConflictoCursoLocal()
                CreacionCursoPendienteEntity(UUID.randomUUID().toString(), cuenta.catalogo,
                    cuenta.perfil.id, seccion.toString(), cursoAnterior).also { dao.insertarPendiente(it) }
            }
        }
        val curso = try {
            remoto.crearCurso(pendiente.seccion, pendiente.clave)
        } catch (e: OperacionDocenteRechazada) {
            // Una respuesta 409 confirma rechazo transaccional; una desconexión no.
            if (e.codigo == 409) base.withTransaction {
                comprobar(cuenta)
                dao.eliminarPendiente(pendiente.clave)
            }
            throw e
        }
        comprobar(cuenta)
        validarCursoCreado(curso, seccion)
        return base.withTransaction {
            comprobar(cuenta)
            val vinculado = dao.buscarVinculo(cuenta.catalogo, cuenta.perfil.id, curso.id)
            val id = if (vinculado != null) {
                check(cursoAnterior == null || vinculado.cursoLocalId == cursoAnterior)
                vinculado.cursoLocalId
            } else {
                val local = if (cursoAnterior != null) {
                    requireNotNull(base.cursoDao().buscarPorIdYDocente(cursoAnterior, cuenta.local)).id
                } else {
                    val locales = base.cursoDao().listarPorDocente(cuenta.local)
                    if (locales.any { PropuestaVinculacionCursos.seccion(it.nombre) == null } ||
                        colision(curso.nombre, locales)) throw ConflictoCursoLocal()
                    base.cursoDao().insertar(CursoEntity(docenteId = cuenta.local, nombre = curso.nombre))
                        .also { if (it <= 0) throw ConflictoCursoLocal() }
                }
                check(dao.buscarPorLocal(local) == null) { "El curso ya está asociado" }
                dao.insertarVinculo(VinculoCursoAutenticadoEntity(cuenta.catalogo, cuenta.perfil.id, curso.id, local))
                local
            }
            dao.eliminarPendiente(pendiente.clave)
            id
        }
    }

    suspend fun reintentarPendientes(docenteLocalId: Long): ResultadoSincronizacionCursos = mutex.withLock {
        val cuenta = cuenta(docenteLocalId)
        val pendientes = base.cursoAutenticadoDao().pendientes(cuenta.catalogo, cuenta.perfil.id)
        for (pendiente in pendientes) enviarCreacion(cuenta, pendiente.seccion.single(), pendiente.cursoLocalId)
        actualizarCuenta(cuenta)
    }

    /**
     * Recupera una respuesta perdida con la misma clave y asocia el resultado al curso elegido.
     * No depende de que los nombres coincidan.
     */
    suspend fun conciliarPendienteConCursoLocal(
        docenteLocalId: Long, cursoLocalId: Long, seccion: Char
    ): Long = mutex.withLock {
        val cuenta = cuenta(docenteLocalId)
        require(seccion in 'A'..'Z')
        val dao = base.cursoAutenticadoDao()
        val pendiente = requireNotNull(dao.buscarPendiente(cuenta.catalogo, cuenta.perfil.id, seccion.toString()))
        check(pendiente.cursoLocalId == null || pendiente.cursoLocalId == cursoLocalId)
        requireNotNull(base.cursoDao().buscarPorIdYDocente(cursoLocalId, cuenta.local))
        check(dao.buscarPorLocal(cursoLocalId) == null) { "El curso ya está asociado" }
        val curso = remoto.crearCurso(pendiente.seccion, pendiente.clave)
        comprobar(cuenta)
        validarCursoCreado(curso, seccion)
        base.withTransaction {
            comprobar(cuenta)
            requireNotNull(base.cursoDao().buscarPorIdYDocente(cursoLocalId, cuenta.local))
            check(dao.buscarPendiente(cuenta.catalogo, cuenta.perfil.id, pendiente.seccion) == pendiente)
            check(dao.buscarPorLocal(cursoLocalId) == null)
            check(dao.buscarVinculo(cuenta.catalogo, cuenta.perfil.id, curso.id) == null)
            dao.insertarVinculo(VinculoCursoAutenticadoEntity(cuenta.catalogo, cuenta.perfil.id, curso.id, cursoLocalId))
            dao.eliminarPendiente(pendiente.clave)
            cursoLocalId
        }
    }

    /** El docente elige ambos IDs; comprobar el listado autorizado antes de asociarlos. */
    suspend fun asociarExistente(docenteLocalId: Long, cursoLocalId: Long, cursoServidorId: Long) = mutex.withLock {
        val cuenta = cuenta(docenteLocalId)
        check(remoto.cursos().any { it.id == cursoServidorId }) { "El curso no pertenece a esta cuenta" }
        comprobar(cuenta)
        base.withTransaction {
            comprobar(cuenta)
            requireNotNull(base.cursoDao().buscarPorIdYDocente(cursoLocalId, cuenta.local))
            val dao = base.cursoAutenticadoDao()
            val local = dao.buscarPorLocal(cursoLocalId)
            val servidor = dao.buscarVinculo(cuenta.catalogo, cuenta.perfil.id, cursoServidorId)
            check(local == null && servidor == null) { "El curso ya está asociado" }
            check(dao.pendientes(cuenta.catalogo, cuenta.perfil.id).isEmpty()) { "Resuelve primero la creación pendiente" }
            dao.insertarVinculo(VinculoCursoAutenticadoEntity(cuenta.catalogo, cuenta.perfil.id, cursoServidorId, cursoLocalId))
        }
    }
}
