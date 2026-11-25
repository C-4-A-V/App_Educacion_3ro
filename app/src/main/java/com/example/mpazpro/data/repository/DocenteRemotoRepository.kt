package com.example.mpazpro.data.repository

import com.example.mpazpro.data.remote.*
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response

object SesionDocenteRemota {
    val repository: DocenteRemotoRepository by lazy {
        DocenteRemotoRepository(ClienteDocenteAutenticado(com.example.mpazpro.BuildConfig.URL_SERVIDOR))
    }
}

class SesionDocenteExpirada : IOException("La sesión del servidor terminó")
class OperacionDocenteRechazada(val codigo: Int) : IOException("El servidor rechazó la operación: $codigo")

/** Acceso remoto explícito e independiente del perfil guardado en Room. */
class DocenteRemotoRepository(private val cliente: ClienteDocenteAutenticado) {
    private val mutex = Mutex()
    private val _sesion = MutableStateFlow<PerfilDocenteRemoto?>(null)
    val sesion = _sesion.asStateFlow()

    suspend fun registrar(correo: String, clave: String): PerfilDocenteRemoto =
        mutex.withLock { obtener(cliente.api.registrar(CredencialesDocenteRemoto(correo.trim(), clave))) }

    suspend fun entrar(correo: String, clave: String): PerfilDocenteRemoto = mutex.withLock {
        cliente.limpiarSesion()
        _sesion.value = null
        val perfil = obtener(cliente.api.entrar(CredencialesDocenteRemoto(correo.trim(), clave)))
        check(perfil.id > 0) { "Identidad de servidor inválida" }
        _sesion.value = perfil
        perfil
    }

    suspend fun recuperarSesion(): PerfilDocenteRemoto = mutex.withLock {
        val perfil = obtener(cliente.api.sesion())
        check(perfil.id > 0) { "Identidad de servidor inválida" }
        _sesion.value = perfil
        perfil
    }

    suspend fun salir() = mutex.withLock {
        _sesion.value = null
        try { cliente.api.salir() }
        finally { cliente.limpiarSesion() }
    }

    suspend fun cursos(): List<CursoDocenteRemoto> = mutex.withLock {
        exigirSesion()
        obtener(cliente.api.cursos()).also { cursos ->
            check(cursos.all { it.id > 0 } && cursos.map { it.id }.distinct().size == cursos.size)
        }
    }

    /** La clave debe persistirse antes del envío y reutilizarse para reintentar la misma operación. */
    suspend fun crearCurso(seccion: String, clave: String): CursoDocenteRemoto = mutex.withLock {
        exigirSesion()
        require(seccion.matches(Regex("[A-Z]")))
        require(UUID.fromString(clave).toString() == clave)
        obtener(cliente.api.crearCurso(clave, CrearCursoDocenteRemoto(seccion)))
    }

    suspend fun publicar(cursoId: Long, clave: String, actividad: SolicitudPublicacion): RespuestaPublicacion =
        mutex.withLock {
            exigirSesion()
            require(cursoId > 0 && actividad.cursoId == cursoId && UUID.fromString(clave).toString() == clave)
            obtener(cliente.api.publicar(cursoId, clave, actividad)).also {
                check(it.actividadId != null && it.actividadId > 0)
            }
        }

    suspend fun resultados(cursoId: Long, actividadId: Long): ResultadoAgregadoRemoto = mutex.withLock {
        exigirSesion()
        require(cursoId > 0 && actividadId > 0)
        obtener(cliente.api.resultados(cursoId, actividadId)).also {
            check(it.actividadId == actividadId && it.correctas in 0..it.finalizadas)
        }
    }

    private fun exigirSesion() {
        if (_sesion.value == null) throw SesionDocenteExpirada()
    }

    private fun <T> obtener(respuesta: Response<T>): T {
        if (respuesta.code() == 401) {
            cliente.limpiarSesion()
            _sesion.value = null
            throw SesionDocenteExpirada()
        }
        if (!respuesta.isSuccessful) throw OperacionDocenteRechazada(respuesta.code())
        return requireNotNull(respuesta.body()) { "Respuesta vacía del servidor" }
    }
}
