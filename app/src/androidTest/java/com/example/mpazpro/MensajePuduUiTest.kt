package com.example.mpazpro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mpazpro.model.MensajeEstudiante
import com.example.mpazpro.model.PerfilEstudiante
import com.example.mpazpro.model.ProgresoEstudiante
import com.example.mpazpro.ui.screens.InicioEstudianteScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MensajePuduUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()

    @Test fun muestraAvisoYPermiteLeerMensaje() {
        var leido = 0L
        compose.setContent {
            InicioEstudianteScreen(
                estudiante = PerfilEstudiante(1, "Sofía", "3 A", null),
                progreso = ProgresoEstudiante(), onSalir = {}, onAbrirAsignatura = {},
                mensajesDocente = listOf(MensajeEstudiante(7, "¡Buen trabajo!", 1, false, "Ana", "Sumemos")),
                onLeerMensaje = { leido = it }
            )
        }
        compose.onNodeWithText("¡Oye, Sofía! Tu docente te envió un mensaje.").assertIsDisplayed().performClick()
        compose.onNodeWithText("✨ Nuevo mensaje de Ana").performClick()
        compose.onNodeWithText("¡Buen trabajo!").assertIsDisplayed()
        compose.onNodeWithText("Actividad: Sumemos").assertIsDisplayed()
        compose.runOnIdle { assertEquals(7L, leido) }
    }
}
