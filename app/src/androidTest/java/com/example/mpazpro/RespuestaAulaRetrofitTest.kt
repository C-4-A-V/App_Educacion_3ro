package com.example.mpazpro

import com.example.mpazpro.data.remote.AulaActividadesApi
import com.example.mpazpro.data.remote.SolicitudRespuestaAula
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RespuestaAulaRetrofitTest {
    @Test fun enviaIntentoSinRutNiPinYRecibeResultado() = runBlocking {
        val servidor = MockWebServer()
        servidor.start()
        try {
            servidor.enqueue(MockResponse().setHeader("Content-Type", "application/json")
                .setBody("""{"numero":1,"respuestaElegida":2,"correcta":false,"finalizada":false,"respuestaCorrecta":null,"explicacion":null}"""))
            val direccion = servidor.url("/").newBuilder().host("127.0.0.1").build()
            val api = Retrofit.Builder().baseUrl(direccion).addConverterFactory(GsonConverterFactory.create())
                .build().create(AulaActividadesApi::class.java)
            val token = "11111111-1111-1111-1111-111111111111"
            val resultado = api.responder("catalogo-prueba", SolicitudRespuestaAula(7, 42, token, 1, 2))
            assertEquals(2, resultado.respuestaElegida)
            assertFalse(resultado.correcta)
            assertFalse(resultado.finalizada)
            assertNull(resultado.respuestaCorrecta)
            assertNull(resultado.explicacion)
            val solicitud = servidor.takeRequest(5, TimeUnit.SECONDS)!!
            assertEquals("/api/aula/respuestas", solicitud.path)
            assertEquals("catalogo-prueba", solicitud.getHeader("X-Catalogo-Id"))
            val cuerpo = JSONObject(solicitud.body.readUtf8())
            assertEquals(token, cuerpo.getString("estudianteToken"))
            assertEquals(1, cuerpo.getInt("numero"))
            assertFalse(cuerpo.has("rut"))
            assertFalse(cuerpo.has("pin"))
        } finally { servidor.shutdown() }
    }
}
