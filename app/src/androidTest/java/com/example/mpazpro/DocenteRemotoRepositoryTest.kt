package com.example.mpazpro

import com.example.mpazpro.data.remote.ClienteDocenteAutenticado
import com.example.mpazpro.data.repository.DocenteRemotoRepository
import com.example.mpazpro.data.repository.SesionDocenteExpirada
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class DocenteRemotoRepositoryTest {
    @Test fun claveDeCreacionSeReutilizaDespuesDeUnaRespuestaPerdida() = runBlocking {
        val servidor = MockWebServer()
        servidor.start()
        try {
            val url = servidor.url("/").newBuilder().host("127.0.0.1").build().toString()
            val repo = DocenteRemotoRepository(ClienteDocenteAutenticado(url))
            servidor.enqueue(MockResponse().addHeader("Set-Cookie", "JSESSIONID=uno; Path=/")
                .setHeader("Content-Type", "application/json").setBody("{\"id\":1,\"correo\":\"a@ejemplo.cl\"}"))
            repo.entrar("a@ejemplo.cl", "clave-segura")
            servidor.takeRequest()
            val clave = java.util.UUID.randomUUID().toString()
            servidor.enqueue(MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AFTER_REQUEST))
            try { repo.crearCurso("A", clave); fail("La pérdida de conexión no confirma la creación") }
            catch (_: java.io.IOException) {}
            assertEquals(clave, servidor.takeRequest().getHeader("Idempotency-Key"))
            servidor.enqueue(MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"id\":12,\"nombre\":\"3.º Básico A\"}"))
            assertEquals(12L, repo.crearCurso("A", clave).id)
            assertEquals(clave, servidor.takeRequest().getHeader("Idempotency-Key"))
            assertEquals(3, servidor.requestCount)
        } finally { servidor.shutdown() }
    }

    @Test fun cookiesExpiracionYCambioDeCuenta() = runBlocking {
        val servidor = MockWebServer()
        servidor.start()
        try {
            val url = servidor.url("/").newBuilder().host("127.0.0.1").build().toString()
            val repo = DocenteRemotoRepository(ClienteDocenteAutenticado(url))
            servidor.enqueue(MockResponse().addHeader("Set-Cookie", "JSESSIONID=primera; Path=/")
                .setHeader("Content-Type", "application/json").setBody("{\"id\":1,\"correo\":\"a@ejemplo.cl\"}"))
            assertEquals("a@ejemplo.cl", repo.entrar("a@ejemplo.cl", "clave-segura").correo)
            assertNull(servidor.takeRequest().getHeader("Cookie"))
            servidor.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("[]"))
            assertTrue(repo.cursos().isEmpty())
            assertEquals("JSESSIONID=primera", servidor.takeRequest().getHeader("Cookie"))
            servidor.enqueue(MockResponse().setResponseCode(401))
            try { repo.cursos(); fail("Debe rechazar la sesión caducada") }
            catch (_: SesionDocenteExpirada) {}
            servidor.takeRequest()
            assertNull(repo.sesion.value)
            servidor.enqueue(MockResponse().addHeader("Set-Cookie", "JSESSIONID=segunda; Path=/")
                .setHeader("Content-Type", "application/json").setBody("{\"id\":2,\"correo\":\"b@ejemplo.cl\"}"))
            assertEquals("b@ejemplo.cl", repo.entrar("b@ejemplo.cl", "clave-segura").correo)
            assertNull(servidor.takeRequest().getHeader("Cookie"))
            servidor.enqueue(MockResponse().setResponseCode(204))
            repo.salir()
            assertEquals("JSESSIONID=segunda", servidor.takeRequest().getHeader("Cookie"))
            assertNull(repo.sesion.value)
        } finally { servidor.shutdown() }
    }
}
