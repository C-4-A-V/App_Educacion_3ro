package com.example.mpazpro

import com.example.mpazpro.data.remote.AccesoEstudiantePrueba
import com.example.mpazpro.data.remote.ClienteSesionEstudiantePrueba
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class SesionEstudiantePruebaRetrofitTest {
    @Test
    fun enviaCookieDeSesionYLaEliminaAlLimpiar() = runBlocking {
        val servidor = MockWebServer()
        servidor.start()
        try {
            val cliente = ClienteSesionEstudiantePrueba(servidor.url("/").newBuilder().host("127.0.0.1").build().toString())
            servidor.enqueue(MockResponse().setHeader("Set-Cookie", "JSESSIONID=prueba; Path=/; HttpOnly")
                .setHeader("Content-Type", "application/json").setBody("{\"autenticado\":true}"))
            assertTrue(cliente.api.entrar(AccesoEstudiantePrueba("20983458-8", "1234")).autenticado)
            val login = servidor.takeRequest()
            assertEquals("/api/sesion-estudiante-prueba/login", login.path)

            servidor.enqueue(MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"autenticado\":true}"))
            assertTrue(cliente.api.estado().isSuccessful)
            assertEquals("JSESSIONID=prueba", servidor.takeRequest().getHeader("Cookie"))

            servidor.enqueue(MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"numero\":1,\"respuestaElegida\":0,\"correcta\":true,\"finalizada\":true,\"respuestaCorrecta\":0,\"explicacion\":\"Bien\"}"))
            assertTrue(cliente.api.responder(com.example.mpazpro.data.remote.RespuestaSesionPrueba(7, 1, 0)).correcta)
            val respuesta = servidor.takeRequest()
            assertEquals("/api/sesion-estudiante-prueba/respuestas", respuesta.path)
            assertEquals("JSESSIONID=prueba", respuesta.getHeader("Cookie"))
            val cuerpo = respuesta.body.readUtf8()
            assertFalse(cuerpo.contains("cursoId"))
            assertFalse(cuerpo.contains("estudianteToken"))

            cliente.limpiarSesion()
            servidor.enqueue(MockResponse().setResponseCode(401))
            assertEquals(401, cliente.api.estado().code())
            assertNull(servidor.takeRequest().getHeader("Cookie"))
        } finally {
            servidor.shutdown()
        }
    }
}
