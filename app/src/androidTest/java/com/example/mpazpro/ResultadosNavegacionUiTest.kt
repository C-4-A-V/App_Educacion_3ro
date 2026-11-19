package com.example.mpazpro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mpazpro.data.local.ActividadEntity
import com.example.mpazpro.data.local.CursoEntity
import com.example.mpazpro.model.*
import com.example.mpazpro.ui.screens.ResumenDocenteContenido
import com.example.mpazpro.ui.theme.DocenteTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ResultadosNavegacionUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()

    private fun actividad(id: Long, titulo: String) = ActividadEntity(
        id = id, cursoId = 1, asignaturaId = "matematica", titulo = titulo,
        instrucciones = "Lee", pregunta = "Pregunta $id", alternativaA = "Cuatro",
        alternativaB = "Tres", alternativaC = "Dos", alternativaD = "Uno",
        respuestaCorrecta = 0, maxIntentos = 3
    )

    private fun resultado(id: Long, estudiante: Long, correcta: Boolean, finalizada: Boolean) =
        ResultadoPreguntaDocente(id, 1, estudiante, "matematica", "Actividad $id", "Pregunta $id",
            3, 1, finalizada, correcta, if (correcta) 0 else 1, 0, !correcta)

    private fun estado() = ResumenDocenteUiState(
        cursos = listOf(CursoEntity(id = 1, docenteId = 1, nombre = "3 A")),
        alumnos = listOf(ResumenAlumno(1, "Sofía", null, 1, emptyMap())),
        actividades = listOf(actividad(1, "Actividad 1"), actividad(2, "Actividad 2"), actividad(3, "Actividad 3")),
        resultados = listOf(resultado(1, 1, true, true), resultado(2, 1, false, true),
            resultado(1, 2, false, false)),
        cargando = false
    )

    @Test fun cursoMuestraOpcionesYResultadosPorAsignatura() {
        compose.setContent {
            DocenteTheme {
                ResumenDocenteContenido(estado(), 1, 0, onVolver = {}, onAbrirCurso = {},
                    onAbrirEstudiante = {}, onReintentar = {}, onEnviar = { _, _ -> })
            }
        }
        compose.onNodeWithText("👩‍🎓 Estudiantes").assertIsDisplayed()
        compose.onNodeWithText("📊 Respuestas en general").performClick()
        compose.onNodeWithText("Matemática").performClick()
        compose.onNodeWithText("Correctas: 50% · 1 de 2").assertExists()
        compose.onNodeWithText("En curso: 1 de 2").assertExists()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Aún no hay respuestas."))
        compose.onNodeWithText("Aún no hay respuestas.").assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        compose.onNodeWithText("Volver a asignaturas").performClick()
        compose.onNodeWithText("Matemática").assertExists()
    }

    @Test fun estudianteFiltraYEnviaRetroalimentacion() {
        var enviado = ""
        compose.setContent {
            DocenteTheme {
                ResumenDocenteContenido(estado(), 1, 1, onVolver = {}, onAbrirCurso = {},
                    onAbrirEstudiante = {}, onReintentar = {}, onEnviar = { _, texto -> enviado = texto })
            }
        }
        compose.onNodeWithText("Matemática").performClick()
        compose.onNodeWithText("Correctas").performClick()
        compose.onNodeWithText("1. Actividad 1").assertExists()
        compose.onNodeWithText("2. Actividad 2").assertDoesNotExist()
        compose.onNodeWithText("Incorrectas").performClick()
        compose.onNodeWithText("2. Actividad 2").assertExists()
        compose.onNodeWithText("Última respuesta: Tres").assertExists()
        compose.onNodeWithText("Escribir mensaje").performClick()
        compose.onNodeWithText("Escribe tu retroalimentación").performTextInput("Practiquemos juntos")
        compose.onNodeWithText("Enviar mensaje").performClick()
        compose.waitForIdle()
        compose.runOnIdle { assertEquals("Practiquemos juntos", enviado) }
    }
}
