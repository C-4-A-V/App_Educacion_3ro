package com.example.mpazpro

import com.example.mpazpro.data.remote.PaisesApi
import com.example.mpazpro.data.repository.AprendeRepository
import com.example.mpazpro.data.repository.TemaPudu
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AprendeRepositoryTest {
    private class ApiFalsa(private val json: String) : PaisesApi {
        override suspend fun obtenerChile(): JsonArray = JsonParser.parseString(json).asJsonArray
    }

    @Test
    fun transformaCapitalRecibidaEnDatoBreve() = runBlocking {
        val api = ApiFalsa("""[{},[{"id":"CHL","capitalCity":"Santiago"}]]""")
        val dato = AprendeRepository(api).capitalDeChile()
        assertEquals("¿Sabías que la capital de Chile es Santiago?", dato.texto)
        assertEquals("Banco Mundial", dato.fuente)
        assertEquals(TemaPudu.CHILE, dato.tema)
    }

    @Test
    fun rechazaRespuestaIncompletaOPaisEquivocado() {
        val api = ApiFalsa("""[{},[{"id":"ARG","capitalCity":"Buenos Aires"}]]""")
        assertThrows(IllegalStateException::class.java) {
            runBlocking { AprendeRepository(api).capitalDeChile() }
        }
    }

    @Test
    fun hayDatosLocalesParaCadaTema() {
        val repository = AprendeRepository(ApiFalsa("[]"))
        TemaPudu.entries.filter { it != TemaPudu.TODOS }.forEach { tema ->
            assertTrue(repository.datosLocales.any { it.tema == tema && it.texto.isNotBlank() })
        }
    }
}
