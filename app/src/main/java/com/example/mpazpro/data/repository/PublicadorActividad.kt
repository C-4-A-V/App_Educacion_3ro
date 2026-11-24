package com.example.mpazpro.data.repository

import androidx.room.withTransaction
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.remote.ActividadesServidorApi
import com.example.mpazpro.data.remote.PublicacionesApi
import com.example.mpazpro.data.remote.SolicitudPublicacion
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal object BloqueoPublicacion { val mutex = Mutex() }

/** Publica una actividad ya guardada; cualquier error mantiene su registro pendiente. */
class PublicadorActividad(
    private val database: MpazDatabase,
    private val catalogoApi: ActividadesServidorApi,
    private val publicacionesApi: PublicacionesApi
) {
    /** Valida toda la selección antes de enviar; los reintentos conservan la clave de cada actividad. */
    suspend fun publicarSeleccion(
        ids: List<Long>, cursoId: Long, asignaturaId: String, docenteId: Long,
        alConfirmar: () -> Unit = {}
    ) {
        require(ids.isNotEmpty() && ids.distinct().size == ids.size)
        database.withTransaction {
            requireNotNull(database.cursoDao().buscarPorIdYDocente(cursoId, docenteId))
            ids.forEach { id ->
                val actividad = requireNotNull(database.actividadDao().buscarPorIdYDocente(id, docenteId))
                require(actividad.cursoId == cursoId && actividad.asignaturaId == asignaturaId)
                check(!database.actividadRetiradaDao().existe(id))
            }
        }
        val repositorio = ActividadRepository(database)
        for (id in ids) {
            repositorio.prepararPublicacion(id, docenteId)
            publicar(id, docenteId)
            alConfirmar()
        }
    }

    suspend fun publicar(actividadId: Long, docenteId: Long): Long =
        BloqueoPublicacion.mutex.withLock { publicarSinBloqueo(actividadId, docenteId) }

    private suspend fun publicarSinBloqueo(actividadId: Long, docenteId: Long): Long {
        check(!database.actividadRetiradaDao().existe(actividadId)) { "La actividad fue retirada" }
        val pendiente = database.publicacionPendienteDao().buscar(actividadId)
            ?: throw IllegalArgumentException("No hay publicación pendiente")
        val actividad = database.actividadDao().buscarPorId(actividadId)
            ?: throw IllegalArgumentException("Actividad no disponible")
        require(database.cursoDao().buscarPorIdYDocente(actividad.cursoId, docenteId) != null) {
            "Curso no disponible"
        }
        pendiente.actividadServidorId?.let { return it }
        val vinculo = database.vinculoCursoDao().buscar(actividad.cursoId)
        check(vinculo != null && vinculo.catalogoId == pendiente.catalogoId &&
            vinculo.cursoServidorId == pendiente.cursoServidorId) { "El vínculo del curso cambió" }

        val identidad = catalogoApi.identidadCatalogo()
        check(identidad.persistente && identidad.identificador == pendiente.catalogoId) {
            "El servidor no corresponde al catálogo vinculado"
        }
        val solicitud = SolicitudPublicacion(
            cursoId = pendiente.cursoServidorId,
            asignaturaId = actividad.asignaturaId,
            titulo = actividad.titulo,
            instrucciones = actividad.instrucciones,
            pregunta = actividad.pregunta,
            alternativaA = actividad.alternativaA,
            alternativaB = actividad.alternativaB,
            alternativaC = actividad.alternativaC,
            alternativaD = actividad.alternativaD,
            respuestaCorrecta = actividad.respuestaCorrecta,
            maxIntentos = actividad.maxIntentos,
            explicacion = actividad.explicacion,
            pista = actividad.pista
        )
        val respuesta = publicacionesApi.publicar(pendiente.clavePublicacion, pendiente.catalogoId, solicitud)
        val remotoId = requireNotNull(respuesta.actividadId)
        require(remotoId > 0) { "Respuesta del servidor inválida" }
        database.withTransaction {
            val actual = database.publicacionPendienteDao().buscar(actividadId)
                ?: throw IllegalStateException("Publicación no disponible")
            check(actual.clavePublicacion == pendiente.clavePublicacion && actual.catalogoId == pendiente.catalogoId &&
                actual.cursoServidorId == pendiente.cursoServidorId &&
                (actual.actividadServidorId == null || actual.actividadServidorId == remotoId)) {
                "La publicación cambió durante el envío"
            }
            if (actual.actividadServidorId == null) database.publicacionPendienteDao().marcarPublicada(actividadId, remotoId)
        }
        return remotoId
    }
}
