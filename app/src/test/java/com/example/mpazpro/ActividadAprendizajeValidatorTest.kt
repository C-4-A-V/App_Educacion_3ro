package com.example.mpazpro

import com.example.mpazpro.model.ActividadFormulario
import com.example.mpazpro.validation.ActividadValidator
import org.junit.Assert.*
import org.junit.Test

class ActividadAprendizajeValidatorTest {
    private fun formulario() = ActividadFormulario(
        titulo = "Sumemos",
        instrucciones = "Lee la pregunta.",
        pregunta = "¿Cuánto es dos más dos?",
        alternativas = listOf("Cuatro", "Tres", "Cinco", "Seis"),
        respuestaCorrecta = 0,
        explicacion = "Dos grupos de dos forman cuatro."
    )

    @Test fun permitePracticaYEvaluacionConExplicacion() {
        assertNull(ActividadValidator.validar(formulario()))
        assertNull(ActividadValidator.validar(formulario().copy(maxIntentos = 1)))
    }

    @Test fun requiereExplicacionAntesDeCrearActividad() {
        assertNotNull(ActividadValidator.validar(formulario().copy(explicacion = "   ")))
        assertNotNull(ActividadValidator.validar(formulario().copy(explicacion = "a".repeat(1001))))
    }

    @Test fun rechazaLimiteDeIntentosFueraDeLasModalidades() {
        assertNotNull(ActividadValidator.validar(formulario().copy(maxIntentos = 2)))
    }

    @Test fun normalizaExplicacionSinPerderSaltosDeLinea() {
        assertEquals(
            "Primero suma.\nLuego comprueba.",
            formulario().copy(explicacion = "  Primero suma.\nLuego comprueba.  ").normalizado().explicacion
        )
    }
}
