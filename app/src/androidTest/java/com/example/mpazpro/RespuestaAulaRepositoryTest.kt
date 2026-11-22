package com.example.mpazpro

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.remote.*
import com.example.mpazpro.data.repository.*
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RespuestaAulaRepositoryTest {
    @Test fun recuperaRespuestaPerdidaSinCambiarElIntento() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val base = Room.inMemoryDatabaseBuilder(contexto, MpazDatabase::class.java).build()
        val archivo = File(contexto.cacheDir, "respuesta_aula_${UUID.randomUUID()}.preferences_pb")
        val trabajo = SupervisorJob()
        try {
            val preferencias = PreferenciasAulaEstudiante(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + trabajo), produceFile = { archivo }
            ))
            val docente = base.docenteDao().insertar(DocenteEntity(
                nombre = "Prueba", correo = "respuesta@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1
            ))
            val curso = base.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            val estudiante = base.estudianteDao().insertar(EstudianteEntity(cursoId = curso, nombre = "Prueba"))
            val catalogo = "11111111-1111-1111-1111-111111111111"
            base.vinculoCursoDao().insertar(VinculoCursoEntity(curso, catalogo, 42))
            val api = AulaFalsa()
            val cursos = AulaEstudianteRepository(base, CatalogoFalso(catalogo), api)
            val repo = RespuestaAulaRepository(cursos, preferencias, api)
            try {
                repo.responder(estudiante, 7, 1, 2)
                fail("Se esperaba una respuesta perdida")
            } catch (_: IOException) {}
            assertEquals(IntentoAulaPendiente(1, 2), preferencias.pendiente(estudiante, catalogo, 7))
            try {
                repo.responder(estudiante, 7, 1, 3)
                fail("No se debe reemplazar un intento pendiente")
            } catch (_: IllegalStateException) {}
            val recuperado = repo.recuperar(estudiante, 7)!!
            assertEquals(1, recuperado.numero)
            assertEquals(2, recuperado.respuestaElegida)
            assertNull(preferencias.pendiente(estudiante, catalogo, 7))
            assertEquals(2, api.envios)
            assertEquals(1, api.tokens.distinct().size)
            assertEquals(recuperado, repo.recuperar(estudiante, 7))

            api.actividades = listOf(7L, 8L, 9L).map { id ->
                ActividadRemotaEstudiante(id, "lenguaje", "Título $id", "Lee", "Pregunta",
                    "A", "B", "C", "D", 3, "")
            }
            api.estados[8] = ResultadoRespuestaAula(1, 0, true, true, 0, "Bien")
            assertEquals(9L, repo.siguiente(estudiante, 7, "lenguaje"))
            api.estados[9] = ResultadoRespuestaAula(1, 1, true, true, 1, "Bien")
            assertNull(repo.siguiente(estudiante, 7, "lenguaje"))
            preferencias.preparar(estudiante, catalogo, 8, IntentoAulaPendiente(1, 0))
            assertEquals(8L, repo.siguiente(estudiante, 7, "lenguaje"))
            assertEquals(2, api.envios) // Buscar la siguiente no envía respuestas pendientes.
        } finally {
            base.close()
            trabajo.cancelAndJoin()
            archivo.delete()
        }
    }

    private class CatalogoFalso(private val id: String) : ActividadesServidorApi {
        override suspend fun identidadCatalogo() = IdentidadCatalogoServidor(id, true)
        override suspend fun listarCursos() = emptyList<CursoCompartidoServidor>()
        override suspend fun crearSiguienteCurso() = error("No se usa")
        override suspend fun listar() = emptyList<ActividadServidor>()
    }

    private class AulaFalsa : AulaActividadesApi {
        var envios = 0
        var actividades = emptyList<ActividadRemotaEstudiante>()
        val estados = mutableMapOf<Long, ResultadoRespuestaAula>()
        val tokens = mutableListOf<String>()
        private var guardado: ResultadoRespuestaAula? = null
        override suspend fun responder(catalogoId: String, solicitud: SolicitudRespuestaAula): ResultadoRespuestaAula {
            envios++
            tokens.add(solicitud.estudianteToken)
            if (guardado == null) {
                guardado = ResultadoRespuestaAula(solicitud.numero, solicitud.respuestaElegida, false, false, null, null)
                throw IOException("Respuesta perdida")
            }
            return requireNotNull(guardado)
        }
        override suspend fun estado(catalogoId: String, solicitud: SolicitudEstadoRespuestaAula): Response<ResultadoRespuestaAula> {
            val resultado = if (solicitud.actividadId == 7L) guardado else estados[solicitud.actividadId]
            return if (resultado == null) Response.success<ResultadoRespuestaAula>(204, null) else Response.success(resultado)
        }
        override suspend fun listar(catalogoId: String, cursoId: Long, asignaturaId: String) = actividades
        override suspend fun obtener(catalogoId: String, actividadId: Long, cursoId: Long): ActividadRemotaEstudiante = error("No se usa")
    }
}
