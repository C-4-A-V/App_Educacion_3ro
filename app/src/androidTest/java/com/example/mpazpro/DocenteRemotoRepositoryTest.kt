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
