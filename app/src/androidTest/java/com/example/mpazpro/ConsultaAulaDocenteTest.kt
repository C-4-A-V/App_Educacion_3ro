package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.remote.*
import com.example.mpazpro.data.repository.ConsultaAulaDocente
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ConsultaAulaDocenteTest {
    @Test fun consultaSoloElCursoVinculadoYRechazaCatalogosOCursosAjenos() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java
        ).build()
        try {
            val docente = db.docenteDao().insertar(DocenteEntity(nombre = "Prueba", correo = "consulta@example.com",
                claveHash = "h", claveSalt = "s", claveIteraciones = 1))
            val otroDocente = db.docenteDao().insertar(DocenteEntity(nombre = "Otro", correo = "otroconsulta@example.com",
                claveHash = "h", claveSalt = "s", claveIteraciones = 1))
            val curso = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            val sinVinculo = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 B"))
            val catalogoId = "11111111-1111-1111-1111-111111111111"
            db.vinculoCursoDao().insertar(VinculoCursoEntity(curso, catalogoId, 42))
            var identidad = catalogoId
            val catalogo = object : ActividadesServidorApi {
                override suspend fun identidadCatalogo() = IdentidadCatalogoServidor(identidad, true)
                override suspend fun listarCursos() = emptyList<CursoCompartidoServidor>()
                override suspend fun crearSiguienteCurso(): CursoCompartidoServidor = error("No se usa")
                override suspend fun listar() = emptyList<ActividadServidor>()
            }
            var llamadas = 0
            var cursoRecibido = 0L
            var asignaturaRecibida = ""
            val pregunta = ActividadRemotaEstudiante(7, "lenguaje", "Lectura", "Lee", "¿Qué es?",
                "Historia", "Árbol", "Número", "Suma", 3, "")
            val aula = object : AulaActividadesApi {
                override suspend fun listar(catalogoId: String, cursoId: Long, asignaturaId: String): List<ActividadRemotaEstudiante> {
                    llamadas++
                    cursoRecibido = cursoId
                    asignaturaRecibida = asignaturaId
                    return listOf(pregunta)
                }
                override suspend fun obtener(catalogoId: String, actividadId: Long, cursoId: Long): ActividadRemotaEstudiante = error("No se usa")
                override suspend fun responder(catalogoId: String, solicitud: SolicitudRespuestaAula): ResultadoRespuestaAula = error("No se usa")
                override suspend fun estado(catalogoId: String, solicitud: SolicitudEstadoRespuestaAula): retrofit2.Response<ResultadoRespuestaAula> = error("No se usa")
            }
            val consulta = ConsultaAulaDocente(db, catalogo, aula)
            assertNull(consulta.listar(sinVinculo, "lenguaje", docente))
            assertEquals(listOf(pregunta), consulta.listar(curso, "lenguaje", docente))
            assertEquals(42L, cursoRecibido)
            assertEquals("lenguaje", asignaturaRecibida)
            try { consulta.listar(curso, "lenguaje", otroDocente); fail("Curso ajeno") }
            catch (_: IllegalArgumentException) {}
            identidad = "22222222-2222-2222-2222-222222222222"
            try { consulta.listar(curso, "lenguaje", docente); fail("Catálogo distinto") }
            catch (_: IllegalStateException) {}
            assertEquals(1, llamadas)
        } finally { db.close() }
    }
}
