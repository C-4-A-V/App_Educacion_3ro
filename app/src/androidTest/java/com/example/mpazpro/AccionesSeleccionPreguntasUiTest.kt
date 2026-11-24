package com.example.mpazpro

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mpazpro.ui.screens.AccionesSeleccionPreguntas
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AccionesSeleccionPreguntasUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()

    @Test fun permiteAccionesMultiplesPeroEditarSoloUnaNoCompartida() {
        val cantidad = mutableIntStateOf(0)
        val editable = mutableStateOf(true)
        var ediciones = 0
        compose.setContent {
            AccionesSeleccionPreguntas(cantidad.intValue, false, true, editable.value,
                onCompartir = {}, onEliminar = {}, onEditar = { ediciones++ })
        }
        compose.onNodeWithText("Compartir seleccionadas").assertIsNotEnabled()
        compose.onNodeWithText("Eliminar seleccionadas").assertIsNotEnabled()
        compose.onNodeWithText("Editar seleccionada").assertIsNotEnabled()
        compose.runOnIdle { cantidad.intValue = 2 }
        compose.onNodeWithText("Compartir seleccionadas").assertIsEnabled()
        compose.onNodeWithText("Eliminar seleccionadas").assertIsEnabled()
        compose.onNodeWithText("Editar seleccionada").assertIsNotEnabled()
        compose.runOnIdle { cantidad.intValue = 1; editable.value = false }
        compose.onNodeWithText("Editar seleccionada").assertIsNotEnabled()
        compose.runOnIdle { editable.value = true }
        compose.onNodeWithText("Editar seleccionada").assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(1, ediciones) }
    }
}
