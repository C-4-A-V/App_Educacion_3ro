package com.example.mpazpro

import androidx.room.Room
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.mpazpro.data.repository.PreferenciasAulaEstudiante
import com.example.mpazpro.data.repository.IntentoAulaPendiente
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import java.io.File
import java.util.UUID
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.remote.*
import com.example.mpazpro.data.repository.ActividadRepository
import com.example.mpazpro.data.repository.AulaEstudianteRepository
import com.example.mpazpro.model.ActividadFormulario
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AulaEstudianteRepositoryTest {
    @Test fun filtraCopiaLocalYCompruebaCatalogoDelCurso() = runBlocking {
        val base = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java
        ).build()
        val archivo = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "aula_test_${UUID.randomUUID()}.preferences_pb")
        val job = SupervisorJob()
        val preferencias = PreferenciasAulaEstudiante(PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + job), produceFile = { archivo }
        ))
        try {
            val docente = base.docenteDao().insertar(DocenteEntity(
                nombre = "Docente", correo = "aula@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1
            ))
            val curso = base.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            val estudiante = base.estudianteDao().insertar(EstudianteEntity(cursoId = curso, nombre = "Prueba"))
            val catalogo = "11111111-1111-1111-1111-111111111111"
            base.vinculoCursoDao().insertar(VinculoCursoEntity(curso, catalogo, 42))
            val local = ActividadRepository(base).crearActividad(curso, "lenguaje", docente,
                ActividadFormulario(titulo = "Lectura", instrucciones = "Lee", pregunta = "¿Qué es un cuento?",
                    alternativas = listOf("Historia", "Número", "Árbol", "Suma"), respuestaCorrecta = 0,
                    maxIntentos = 3, explicacion = "Es una historia"))
            base.publicacionPendienteDao().marcarPublicada(local, 2)
            val api = AulaFalsa()
            val catalogoApi = CatalogoFalso(catalogo)
            val repo = AulaEstudianteRepository(base, catalogoApi, api)
            val recibidas = repo.listar(estudiante, "lenguaje")!!
            // Sin preferencias no se puede comprobar el progreso remoto, así que se conservan ambas.
            assertEquals(listOf(2L, 3L), recibidas.map { it.id })
            assertEquals(42L, api.cursoConsultado)
            assertEquals(catalogo, api.catalogoConsultado)

            val conPreferencias = AulaEstudianteRepository(base, catalogoApi, api, preferencias)
            assertEquals(listOf(2L, 3L), conPreferencias.listar(estudiante, "lenguaje")!!.map { it.id })
            preferencias.token(estudiante, catalogo)
            api.estado = 204
            assertEquals(listOf(2L, 3L), conPreferencias.listar(estudiante, "lenguaje")!!.map { it.id })
            api.estado = 200
            assertEquals(listOf(2L, 3L), conPreferencias.listar(estudiante, "lenguaje")!!.map { it.id })
            api.estado = 500
            assertEquals(listOf(2L, 3L), conPreferencias.listar(estudiante, "lenguaje")!!.map { it.id })
            preferencias.preparar(estudiante, catalogo, 2, IntentoAulaPendiente(1, 0))
            api.estado = 204
            assertEquals(listOf(2L, 3L), conPreferencias.listar(estudiante, "lenguaje")!!.map { it.id })
            val consultasPrevias = api.consultas

            api.maxIntentos = 2
            try {
                repo.listar(estudiante, "lenguaje")
                fail("No debe mostrar una actividad con intentos inválidos")
            } catch (_: IllegalStateException) {}
            api.maxIntentos = 3

            catalogoApi.identidad = "22222222-2222-2222-2222-222222222222"
            try {
                repo.listar(estudiante, "lenguaje")
                fail("No debe consultar otro catálogo")
            } catch (_: IllegalStateException) {}
            assertEquals(consultasPrevias + 1, api.consultas)
        } finally { base.close(); job.cancelAndJoin(); archivo.delete() }
    }

    private class CatalogoFalso(var identidad: String) : ActividadesServidorApi {
        override suspend fun identidadCatalogo() = IdentidadCatalogoServidor(identidad, true)
        override suspend fun listarCursos() = emptyList<CursoCompartidoServidor>()
        override suspend fun crearSiguienteCurso() = error("No se usa")
        override suspend fun listar() = emptyList<ActividadServidor>()
    }

    private class AulaFalsa : AulaActividadesApi {
        var consultas = 0
        var maxIntentos = 3
        var estado = 204
        var cursoConsultado = 0L
        var catalogoConsultado = ""
        override suspend fun listar(catalogoId: String, cursoId: Long, asignaturaId: String): List<ActividadRemotaEstudiante> {
            consultas++
            cursoConsultado = cursoId
            catalogoConsultado = catalogoId
            return listOf(pregunta(2), pregunta(3))
        }
        private fun pregunta(id: Long) = ActividadRemotaEstudiante(
            id, "lenguaje", "Lectura", "Lee", "¿Qué es un cuento?", "Historia", "Número", "Árbol", "Suma", maxIntentos, ""
        )
        override suspend fun obtener(catalogoId: String, actividadId: Long, cursoId: Long) = pregunta(actividadId)
        override suspend fun responder(catalogoId: String, solicitud: SolicitudRespuestaAula): ResultadoRespuestaAula = error("No se usa")
        override suspend fun estado(catalogoId: String, solicitud: SolicitudEstadoRespuestaAula): retrofit2.Response<ResultadoRespuestaAula> = when (estado) {
            204 -> retrofit2.Response.success<ResultadoRespuestaAula>(204, null)
            200 -> retrofit2.Response.success(ResultadoRespuestaAula(1, 0, false, false, null, null))
            else -> retrofit2.Response.error(500, okhttp3.ResponseBody.create(null, "error"))
        }
    }
}
