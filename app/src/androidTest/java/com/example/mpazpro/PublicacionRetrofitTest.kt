package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.remote.*
import com.example.mpazpro.data.repository.ActividadRepository
import com.example.mpazpro.data.repository.PublicadorActividad
import com.example.mpazpro.model.ActividadFormulario
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PublicacionRetrofitTest {
    @Test fun asegurarCursoEnviaSeccionEIdentidad() = runBlocking {
        val servidor = MockWebServer()
        servidor.start()
        try {
            servidor.enqueue(MockResponse().setResponseCode(201).setHeader("Content-Type", "application/json")
                .setBody("""{"id":7,"nombre":"3° B"}"""))
            val url = servidor.url("/").newBuilder().host("127.0.0.1").build()
            val api = Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create())
                .build().create(AsegurarCursoApi::class.java)
            val curso = api.asegurar("B", "catalogo-prueba")
            assertEquals(7L, curso.id)
            val solicitud = servidor.takeRequest(5, TimeUnit.SECONDS)!!
            assertEquals("/api/cursos-compartidos/B/asegurar", solicitud.path)
            assertEquals("catalogo-prueba", solicitud.getHeader("X-Catalogo-Id"))
        } finally { servidor.shutdown() }
    }

    @Test fun enviaRetiroMultipleYProcesaRespuestaSinContenido() = runBlocking {
        val servidor = MockWebServer()
        servidor.start()
        try {
            servidor.enqueue(MockResponse().setResponseCode(204))
            val retrofit = Retrofit.Builder().baseUrl(servidor.url("/").newBuilder().host("127.0.0.1").build())
                .addConverterFactory(GsonConverterFactory.create()).build()
            val api = retrofit.create(RetiroPublicacionesApi::class.java)
            val clave = "11111111-1111-1111-1111-111111111111"
            api.retirar("catalogo", SolicitudRetiro(42, listOf(clave)))
            val solicitud = servidor.takeRequest(5, TimeUnit.SECONDS)!!
            assertEquals("/api/publicaciones/retirar", solicitud.path)
            assertEquals("catalogo", solicitud.getHeader("X-Catalogo-Id"))
            val json = JSONObject(solicitud.body.readUtf8())
            assertEquals(42L, json.getLong("cursoId"))
            assertEquals(clave, json.getJSONArray("claves").getString(0))
        } finally { servidor.shutdown() }
    }

    @Test fun enviaActividadConIdentidadYClaveSinExponerDatosDeEstudiantes() = runBlocking {
        val servidor = MockWebServer()
        servidor.start()
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val base = Room.inMemoryDatabaseBuilder(contexto, MpazDatabase::class.java).build()
        try {
            val identidad = "11111111-1111-1111-1111-111111111111"
            val docente = base.docenteDao().insertar(DocenteEntity(
                nombre = "Docente", correo = "retrofit@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1
            ))
            val curso = base.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            base.vinculoCursoDao().insertar(VinculoCursoEntity(curso, identidad, 42))
            val actividad = ActividadRepository(base).crearActividad(curso, "lenguaje", docente,
                ActividadFormulario(titulo = "Lectura", instrucciones = "Lee", pregunta = "¿Qué es un cuento?",
                    alternativas = listOf("Historia", "Número", "Árbol", "Suma"), respuestaCorrecta = 0,
                    maxIntentos = 3, explicacion = "Es una historia"))
            val pendiente = base.publicacionPendienteDao().buscar(actividad)!!
            servidor.enqueue(MockResponse().setHeader("Content-Type", "application/json")
                .setBody("""{"identificador":"$identidad","persistente":true}"""))
            servidor.enqueue(MockResponse().setResponseCode(201).setHeader("Content-Type", "application/json")
                .setBody("""{"actividadId":7,"creada":true}"""))
            val direccion = servidor.url("/").newBuilder().host("127.0.0.1").build()
            val retrofit = Retrofit.Builder().baseUrl(direccion).addConverterFactory(GsonConverterFactory.create()).build()
            val publicador = PublicadorActividad(base, retrofit.create(ActividadesServidorApi::class.java),
                retrofit.create(PublicacionesApi::class.java))

            assertEquals(7L, publicador.publicar(actividad, docente))
            val consulta = servidor.takeRequest(5, TimeUnit.SECONDS)!!
            val publicacion = servidor.takeRequest(5, TimeUnit.SECONDS)!!
            assertEquals("/api/catalogo/identidad", consulta.path)
            assertEquals("/api/publicaciones", publicacion.path)
            assertEquals(pendiente.clavePublicacion, publicacion.getHeader("Idempotency-Key"))
            assertEquals(identidad, publicacion.getHeader("X-Catalogo-Id"))
            val datos = JSONObject(publicacion.body.readUtf8())
            assertEquals(42L, datos.getLong("cursoId"))
            assertEquals("Lectura", datos.getString("titulo"))
            assertFalse(datos.has("rut"))
            assertFalse(datos.has("pin"))
            assertEquals(7L, base.publicacionPendienteDao().buscar(actividad)!!.actividadServidorId)
        } finally {
            base.close()
            servidor.shutdown()
        }
    }
}
