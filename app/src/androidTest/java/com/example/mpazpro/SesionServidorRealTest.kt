package com.example.mpazpro

import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.remote.AccesoEstudiantePrueba
import com.example.mpazpro.data.remote.ClienteSesionEstudiantePrueba
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Prueba explícita y de solo lectura, requiere servidor local y argumentos de instrumentación. */
class SesionServidorRealTest {
    @Test fun autenticaConsultaYCierraSesion() = runBlocking {
        val args = InstrumentationRegistry.getArguments()
        val url = args.getString("aulaUrl")
        val rut = args.getString("aulaRut")
        val pin = args.getString("aulaPin")
        assumeTrue("Prueba real no solicitada", !url.isNullOrBlank() && !rut.isNullOrBlank() && !pin.isNullOrBlank())
        val cliente = ClienteSesionEstudiantePrueba(requireNotNull(url))
        assertTrue(cliente.api.entrar(AccesoEstudiantePrueba(requireNotNull(rut), requireNotNull(pin))).autenticado)
        try {
            assertTrue(cliente.api.estado().isSuccessful)
            val preguntas = cliente.api.actividades("lenguaje")
            assertTrue(preguntas.all { it.asignaturaId == "lenguaje" })
        } finally {
            assertTrue(cliente.api.salir().isSuccessful)
            cliente.limpiarSesion()
        }
        assertEquals(401, cliente.api.estado().code())
    }
}
