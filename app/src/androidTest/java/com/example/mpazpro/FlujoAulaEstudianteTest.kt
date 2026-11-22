package com.example.mpazpro

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.remote.*
import com.example.mpazpro.data.repository.*
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FlujoAulaEstudianteTest {
    @Test fun recibeYRespondePreguntaDelCursoSinEnviarDatosPersonales() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val base = Room.inMemoryDatabaseBuilder(contexto, MpazDatabase::class.java).build()
        val archivo = File(contexto.cacheDir, "flujo_aula_${UUID.randomUUID()}.preferences_pb")
        val trabajo = SupervisorJob()
        val servidor = MockWebServer()
        servidor.start()
        try {
            val catalogo = "11111111-1111-1111-1111-111111111111"
            val identidadJson = """{"identificador":"$catalogo","persistente":true}"""
            val preguntaJson = """{"id":7,"asignaturaId":"lenguaje","titulo":"Lectura","instrucciones":"Lee","pregunta":"¿Qué es un cuento?","alternativaA":"Historia","alternativaB":"Número","alternativaC":"Árbol","alternativaD":"Suma","maxIntentos":3,"pista":"Piensa en un relato"}"""
            fun respuesta(cuerpo: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(cuerpo)
            servidor.enqueue(respuesta(identidadJson))
            servidor.enqueue(respuesta("[$preguntaJson]"))
            servidor.enqueue(respuesta(identidadJson))
            servidor.enqueue(respuesta(preguntaJson))
            servidor.enqueue(respuesta(identidadJson))
            servidor.enqueue(MockResponse().setResponseCode(204))
            servidor.enqueue(respuesta(identidadJson))
            servidor.enqueue(respuesta("""{"numero":1,"respuestaElegida":0,"correcta":true,"finalizada":true,"respuestaCorrecta":0,"explicacion":"Es una historia."}"""))

            val docente = base.docenteDao().insertar(DocenteEntity(
                nombre = "Prueba", correo = "flujo@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1
            ))
            val curso = base.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            val estudiante = base.estudianteDao().insertar(EstudianteEntity(cursoId = curso, nombre = "Prueba"))
            base.vinculoCursoDao().insertar(VinculoCursoEntity(curso, catalogo, 42))
            val direccion = servidor.url("/").newBuilder().host("127.0.0.1").build()
            val retrofit = Retrofit.Builder().baseUrl(direccion).addConverterFactory(GsonConverterFactory.create()).build()
            val catalogoApi = retrofit.create(ActividadesServidorApi::class.java)
            val aulaApi = retrofit.create(AulaActividadesApi::class.java)
            val aula = AulaEstudianteRepository(base, catalogoApi, aulaApi)
            val preferencias = PreferenciasAulaEstudiante(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + trabajo), produceFile = { archivo }
            ))
            val respuestas = RespuestaAulaRepository(aula, preferencias, aulaApi)

            assertEquals(listOf(7L), aula.listar(estudiante, "lenguaje")!!.map { it.id })
            assertEquals("Lectura", aula.obtener(estudiante, 7)!!.titulo)
            assertNull(respuestas.recuperar(estudiante, 7))
            val resultado = respuestas.responder(estudiante, 7, 1, 0)
            assertTrue(resultado.correcta)
            assertTrue(resultado.finalizada)
            assertNull(preferencias.pendiente(estudiante, catalogo, 7))

            val solicitudes = (1..8).map { servidor.takeRequest(5, TimeUnit.SECONDS)!! }
            val solicitudRespuesta = solicitudes.last()
            assertEquals("/api/aula/respuestas", solicitudRespuesta.path)
            val cuerpo = JSONObject(solicitudRespuesta.body.readUtf8())
            assertEquals(42L, cuerpo.getLong("cursoId"))
            assertFalse(cuerpo.has("rut"))
            assertFalse(cuerpo.has("pin"))
            assertFalse(cuerpo.has("nombre"))
        } finally {
            base.close()
            trabajo.cancelAndJoin()
            archivo.delete()
            servidor.shutdown()
        }
    }
}
