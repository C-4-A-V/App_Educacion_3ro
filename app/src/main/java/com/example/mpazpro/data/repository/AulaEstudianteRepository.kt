package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.remote.ActividadRemotaEstudiante
import com.example.mpazpro.data.remote.ActividadesServidorApi
import com.example.mpazpro.data.remote.AulaActividadesApi
import com.example.mpazpro.data.remote.SolicitudEstadoRespuestaAula
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import com.example.mpazpro.model.Asignatura

data class ContextoAulaEstudiante(val cursoLocalId: Long, val catalogoId: String, val cursoServidorId: Long)

/** Solo consulta actividades; no transmite RUT, PIN ni datos personales del estudiante. */
class AulaEstudianteRepository(
    private val database: MpazDatabase,
    private val catalogoApi: ActividadesServidorApi,
    private val aulaApi: AulaActividadesApi,
    private val preferencias: PreferenciasAulaEstudiante? = null
) {
    suspend fun contexto(estudianteId: Long): ContextoAulaEstudiante? {
        val curso = database.estudianteDao().buscarCursoId(estudianteId) ?: return null
        val vinculo = database.vinculoCursoDao().buscar(curso) ?: return null
        val identidad = catalogoApi.identidadCatalogo()
        check(identidad.persistente && identidad.identificador == vinculo.catalogoId) {
            "El servidor no corresponde al curso vinculado"
        }
        return ContextoAulaEstudiante(curso, vinculo.catalogoId, vinculo.cursoServidorId)
    }

    suspend fun listar(estudianteId: Long, asignaturaId: String): List<ActividadRemotaEstudiante>? {
        require(Asignatura.buscarPorId(asignaturaId) != null)
        val contexto = contexto(estudianteId) ?: return null
        val remotas = aulaApi.listar(contexto.catalogoId, contexto.cursoServidorId, asignaturaId)
        check(remotas.all { valida(it) && it.asignaturaId == asignaturaId } &&
            remotas.map { it.id }.distinct().size == remotas.size) { "Lista de actividades inválida" }
        // Las compartidas son la fuente de actividades aprobadas para el estudiante.
        return remotas
    }

    suspend fun obtener(estudianteId: Long, actividadServidorId: Long): ActividadRemotaEstudiante? {
        require(actividadServidorId > 0)
        val contexto = contexto(estudianteId) ?: return null
        return aulaApi.obtener(contexto.catalogoId, actividadServidorId, contexto.cursoServidorId)
            .also { check(it.id == actividadServidorId && valida(it)) }
    }

    private fun valida(actividad: ActividadRemotaEstudiante): Boolean {
        return actividad.id > 0 && Asignatura.buscarPorId(actividad.asignaturaId) != null &&
            actividad.maxIntentos in listOf(1, 3) &&
            actividad.titulo.isNotBlank() && actividad.instrucciones.isNotBlank() &&
            actividad.pregunta.isNotBlank() &&
            listOf(actividad.alternativaA, actividad.alternativaB, actividad.alternativaC, actividad.alternativaD)
                .all { it.isNotBlank() } &&
            (actividad.maxIntentos == 3 || actividad.pista.isEmpty())
    }
}
