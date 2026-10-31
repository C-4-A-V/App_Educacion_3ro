package com.example.mpazpro

import com.example.mpazpro.data.local.BancoActividades
import com.example.mpazpro.model.ActividadFormulario
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.validation.ActividadValidator
import org.junit.Assert.*
import org.junit.Test

class ActividadCamposTest {
    @Test fun formularioVacioInformaTodosLosCampos() {
        val errores = ActividadValidator.errores(ActividadFormulario())
        assertNotNull(errores.titulo)
        assertNotNull(errores.instrucciones)
        assertNotNull(errores.pregunta)
        assertNotNull(errores.explicacion)
        assertNotNull(errores.respuestaCorrecta)
        assertTrue(errores.alternativas.all { it != null })
    }

    @Test fun duplicadosSeMarcanEnAmbasAlternativas() {
        val errores = ActividadValidator.errores(ActividadFormulario(
            alternativas = listOf(" Bosque ", "bosque", "Río", "Mar")
        ))
        assertNotNull(errores.alternativas[0])
        assertNotNull(errores.alternativas[1])
        assertNull(errores.alternativas[2])
    }

    @Test fun bancoTieneDiezPreguntasValidasPorAsignatura() {
        assertEquals(40, BancoActividades.preguntas.size)
        Asignatura.entries.forEach { asignatura ->
            val preguntas = BancoActividades.preguntas.filter { it.asignatura == asignatura }
            assertEquals(10, preguntas.size)
            assertEquals(10, preguntas.map { it.titulo }.distinct().size)
            preguntas.forEach {
                assertNull(it.titulo, ActividadValidator.validar(ActividadFormulario(
                    titulo = it.titulo, instrucciones = "Lee con calma y elige una alternativa.",
                    pregunta = it.enunciado, alternativas = it.opciones,
                    respuestaCorrecta = it.correcta, explicacion = it.explicacion
                )))
            }
        }
    }
}
