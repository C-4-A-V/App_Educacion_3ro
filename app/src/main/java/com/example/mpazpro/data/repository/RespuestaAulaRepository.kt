package com.example.mpazpro.data.repository

import com.example.mpazpro.data.remote.AulaActividadesApi
import com.example.mpazpro.data.remote.ResultadoRespuestaAula
import com.example.mpazpro.data.remote.SolicitudEstadoRespuestaAula
import com.example.mpazpro.data.remote.SolicitudRespuestaAula
import java.io.IOException

/** Una respuesta pendiente se conserva y se reenvía sin cambiar número ni alternativa. */
class RespuestaAulaRepository(
    private val cursos: AulaEstudianteRepository,
    private val preferencias: PreferenciasAulaEstudiante,
    private val api: AulaActividadesApi
) {
    suspend fun recuperar(estudianteId: Long, actividadId: Long): ResultadoRespuestaAula? {
        val contexto = requireNotNull(cursos.contexto(estudianteId)) { "Curso sin vínculo" }
        val token = preferencias.token(estudianteId, contexto.catalogoId)
        val pendiente = preferencias.pendiente(estudianteId, contexto.catalogoId, actividadId)
        if (pendiente != null) return enviar(contexto, estudianteId, actividadId, token, pendiente)
        val respuesta = api.estado(contexto.catalogoId, SolicitudEstadoRespuestaAula(
            actividadId, contexto.cursoServidorId, token
        ))
        if (respuesta.code() == 204) return null
        if (!respuesta.isSuccessful) throw IOException("No se pudo recuperar el intento")
        return validar(requireNotNull(respuesta.body()))
    }

    /** Busca otra pregunta visible sin enviar respuestas pendientes ni repetir las finalizadas. */
    suspend fun siguiente(estudianteId: Long, actividadId: Long, asignaturaId: String): Long? {
        val contexto = requireNotNull(cursos.contexto(estudianteId)) { "Curso sin vínculo" }
        val actividades = requireNotNull(cursos.listar(estudianteId, asignaturaId))
        val indice = actividades.indexOfFirst { it.id == actividadId }
        val ordenadas = if (indice < 0) actividades else actividades.drop(indice + 1) + actividades.take(indice)
        val token = preferencias.token(estudianteId, contexto.catalogoId)
        for (actividad in ordenadas) {
            if (actividad.id == actividadId) continue
            if (preferencias.pendiente(estudianteId, contexto.catalogoId, actividad.id) != null) return actividad.id
            val estado = api.estado(contexto.catalogoId, SolicitudEstadoRespuestaAula(
                actividad.id, contexto.cursoServidorId, token
            ))
            if (estado.code() == 204) return actividad.id
            if (!estado.isSuccessful) throw IOException("No se pudo consultar la siguiente pregunta")
            if (!validar(requireNotNull(estado.body())).finalizada) return actividad.id
        }
        return null
    }

    suspend fun responder(estudianteId: Long, actividadId: Long, numero: Int, respuesta: Int): ResultadoRespuestaAula {
        val contexto = requireNotNull(cursos.contexto(estudianteId)) { "Curso sin vínculo" }
        val token = preferencias.token(estudianteId, contexto.catalogoId)
        val pendiente = IntentoAulaPendiente(numero, respuesta)
        preferencias.preparar(estudianteId, contexto.catalogoId, actividadId, pendiente)
        return enviar(contexto, estudianteId, actividadId, token, pendiente)
    }

    private suspend fun enviar(
        contexto: ContextoAulaEstudiante,
        estudianteId: Long,
        actividadId: Long,
        token: String,
        pendiente: IntentoAulaPendiente
    ): ResultadoRespuestaAula {
        val resultado = validar(api.responder(contexto.catalogoId, SolicitudRespuestaAula(
            actividadId, contexto.cursoServidorId, token, pendiente.numero, pendiente.respuestaElegida
        )))
        check(resultado.numero == pendiente.numero && resultado.respuestaElegida == pendiente.respuestaElegida) {
            "La respuesta recibida no corresponde al intento"
        }
        preferencias.confirmar(estudianteId, contexto.catalogoId, actividadId, pendiente)
        return resultado
    }

    private fun validar(resultado: ResultadoRespuestaAula): ResultadoRespuestaAula {
        check(resultado.numero in 1..3 && resultado.respuestaElegida in 0..3)
        check(!resultado.correcta || resultado.finalizada)
        if (resultado.finalizada) {
            check(resultado.respuestaCorrecta in 0..3)
            check(resultado.correcta == (resultado.respuestaElegida == resultado.respuestaCorrecta))
        } else {
            check(resultado.respuestaCorrecta == null && resultado.explicacion == null)
        }
        return resultado
    }
}
