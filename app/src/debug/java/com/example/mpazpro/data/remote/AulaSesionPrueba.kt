package com.example.mpazpro.data.remote

import com.example.mpazpro.data.repository.IntentoAulaPendiente
import com.example.mpazpro.data.repository.PreferenciasAulaEstudiante
import java.io.IOException

/** Operaciones de aula de prueba asociadas a un perfil local y una sesión explícita. */
class AulaSesionPrueba(
    private val api: SesionEstudiantePruebaApi,
    private val preferencias: PreferenciasAulaEstudiante,
    private val estudianteId: Long,
    private val catalogoId: String
) {
    suspend fun listar(asignaturaId: String): List<ActividadRemotaEstudiante> {
        val actividades = api.actividades(asignaturaId)
        check(actividades.all { it.id > 0 && it.asignaturaId == asignaturaId })
        check(actividades.map { it.id }.distinct().size == actividades.size)
        return actividades
    }

    suspend fun progreso(asignaturaId: String? = null): ProgresoSesionPrueba {
        val progreso = api.progreso(asignaturaId)
        check(progreso.total >= 0 && progreso.completadas in 0..progreso.total &&
            progreso.correctas in 0..progreso.completadas) { "Progreso inválido" }
        return progreso
    }

    suspend fun recuperar(actividadId: Long): ResultadoRespuestaAula? {
        val pendiente = preferencias.pendiente(estudianteId, catalogoId, actividadId)
        if (pendiente != null) return enviar(actividadId, pendiente)
        val respuesta = api.resultado(actividadId)
        if (respuesta.code() == 204) return null
        if (!respuesta.isSuccessful) throw IOException("No se pudo consultar el resultado")
        return validar(requireNotNull(respuesta.body()))
    }

    suspend fun responder(actividadId: Long, numero: Int, respuesta: Int): ResultadoRespuestaAula {
        require(actividadId > 0)
        val pendiente = IntentoAulaPendiente(numero, respuesta)
        preferencias.preparar(estudianteId, catalogoId, actividadId, pendiente)
        return enviar(actividadId, pendiente)
    }

    private suspend fun enviar(id: Long, intento: IntentoAulaPendiente): ResultadoRespuestaAula {
        val resultado = validar(api.responder(RespuestaSesionPrueba(id, intento.numero, intento.respuestaElegida)))
        check(resultado.numero == intento.numero && resultado.respuestaElegida == intento.respuestaElegida)
        preferencias.confirmar(estudianteId, catalogoId, id, intento)
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
