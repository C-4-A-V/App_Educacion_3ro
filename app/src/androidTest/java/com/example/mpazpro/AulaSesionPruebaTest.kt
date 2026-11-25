package com.example.mpazpro

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.remote.AulaSesionPrueba
import com.example.mpazpro.data.remote.ClienteSesionEstudiantePrueba
import com.example.mpazpro.data.repository.IntentoAulaPendiente
import com.example.mpazpro.data.repository.PreferenciasAulaEstudiante
import java.io.File
import java.util.UUID
import kotlinx.coroutines.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class AulaSesionPruebaTest {
    @Test fun conservaRespuestaCuandoFallaYLaReenviaSinAlterarla() = runBlocking {
        val trabajo = SupervisorJob()
        val archivo = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,
            "aula_sesion_${UUID.randomUUID()}.preferences_pb")
        val servidor = MockWebServer()
        servidor.start()
        try {
            val preferencias = PreferenciasAulaEstudiante(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + trabajo), produceFile = { archivo }))
            val catalogo = UUID.randomUUID().toString()
            val url = servidor.url("/").newBuilder().host("127.0.0.1").build().toString()
            val aula = AulaSesionPrueba(ClienteSesionEstudiantePrueba(url).api, preferencias, 1L, catalogo)
            servidor.enqueue(MockResponse().setResponseCode(503))
            try {
                aula.responder(7, 1, 0)
                fail("Debe conservar el intento si falla la conexión")
            } catch (_: retrofit2.HttpException) {}
            assertEquals(IntentoAulaPendiente(1, 0), preferencias.pendiente(1, catalogo, 7))
            val primera = servidor.takeRequest().body.readUtf8()
            servidor.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(
                "{\"numero\":1,\"respuestaElegida\":0,\"correcta\":true,\"finalizada\":true,\"respuestaCorrecta\":0,\"explicacion\":\"Bien\"}"))
            assertTrue(requireNotNull(aula.recuperar(7)).correcta)
            assertEquals(primera, servidor.takeRequest().body.readUtf8())
            assertNull(preferencias.pendiente(1, catalogo, 7))
            servidor.enqueue(MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"total\":2,\"completadas\":1,\"correctas\":1}"))
            val progreso = aula.progreso("lenguaje")
            assertEquals(2, progreso.total)
            assertEquals(1, progreso.completadas)
            assertEquals(1, progreso.correctas)
            assertEquals("/api/sesion-estudiante-prueba/actividades/progreso?asignaturaId=lenguaje",
                servidor.takeRequest().path)
        } finally {
            trabajo.cancelAndJoin()
            servidor.shutdown()
            archivo.delete()
        }
    }
}
