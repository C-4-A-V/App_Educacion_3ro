package com.example.mpazpro

import com.example.mpazpro.data.repository.TemaPudu
import com.example.mpazpro.ui.viewmodel.BolsaCuriosidades
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BolsaCuriosidadesTest {
    @Test
    fun recorreTodosAntesDeRepetirYEvitaRepeticionEnElCambioDeCiclo() {
        val bolsa = BolsaCuriosidades()
        val ids = listOf("a", "b", "c")
        val vistos = (1..3).map {
            bolsa.siguiente(TemaPudu.CHILE, ids).also { bolsa.mostrado(TemaPudu.CHILE, it) }
        }
        assertEquals(ids.toSet(), vistos.toSet())
        val siguiente = bolsa.siguiente(TemaPudu.CHILE, ids)
        assertNotEquals(vistos.last(), siguiente)
    }

    @Test
    fun conservaUnCicloIndependientePorTema() {
        val bolsa = BolsaCuriosidades()
        val ids = listOf("a", "b", "c")
        val primero = bolsa.siguiente(TemaPudu.CHILE, ids)
        bolsa.mostrado(TemaPudu.CHILE, primero)
        bolsa.siguiente(TemaPudu.TODOS, ids)
        assertNotEquals(primero, bolsa.siguiente(TemaPudu.CHILE, ids))
    }

    @Test
    fun puedeUsarUnDatoLocalCuandoFallaLaApi() {
        val bolsa = BolsaCuriosidades()
        val ids = listOf("local", "capital")
        assertEquals("local", bolsa.siguiente(TemaPudu.CHILE, ids, setOf("capital")))
    }
}
