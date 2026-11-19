package com.example.mpazpro

import com.example.mpazpro.model.ResultadoPreguntaDocente
import com.example.mpazpro.model.estadisticaPregunta
import org.junit.Assert.assertEquals
import org.junit.Test

class EstadisticaPreguntaTest {
    private fun respuesta(estudiante: Long, correcta: Boolean, finalizada: Boolean, error: Boolean) =
        ResultadoPreguntaDocente(1, 1, estudiante, "matematica", "Sumar", "Pregunta",
            3, 1, finalizada, correcta, 0, 0, error)

    @Test fun diferenciaAciertoFinalErroresPreviosYRespuestasEnCurso() {
        val calculo = estadisticaPregunta(listOf(
            respuesta(1, true, true, true),
            respuesta(2, false, true, true),
            respuesta(3, false, false, true)
        ))
        assertEquals(3, calculo.respondieron)
        assertEquals(1, calculo.correctas)
        assertEquals(1, calculo.terminaronSinAcertar)
        assertEquals(3, calculo.tuvieronErrores)
        assertEquals(1, calculo.enCurso)
    }

    @Test fun sinRespuestasNoHayResultados() {
        val calculo = estadisticaPregunta(emptyList())
        assertEquals(0, calculo.respondieron)
        assertEquals(0, calculo.correctas)
        assertEquals(0, calculo.terminaronSinAcertar)
    }
}
