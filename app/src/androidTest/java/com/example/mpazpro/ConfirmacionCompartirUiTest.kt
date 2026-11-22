package com.example.mpazpro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mpazpro.ui.screens.ConfirmacionCompartirPreguntas
import com.example.mpazpro.ui.theme.DocenteTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ConfirmacionCompartirUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()

    @Test fun cancelarNoComparteYConfirmarEjecutaUnaVez() {
        var compartidas = 0
        var canceladas = 0
        compose.setContent {
            DocenteTheme {
                ConfirmacionCompartirPreguntas(2, "Lenguaje", "3.º Básico B", true,
                    onCancelar = { canceladas++ }, onConfirmar = { compartidas++ })
            }
        }
        compose.onNodeWithText("Se compartirán 2 pregunta(s) de Lenguaje con 3.º Básico B.").assertIsDisplayed()
        compose.onNodeWithText("Cancelar").performClick()
        compose.runOnIdle { assertEquals(1, canceladas); assertEquals(0, compartidas) }
        compose.onNodeWithText("Confirmar y compartir").performClick()
        compose.runOnIdle { assertEquals(1, compartidas) }
    }

    @Test fun destinoNoDisponibleNoPermiteConfirmar() {
        compose.setContent {
            DocenteTheme { ConfirmacionCompartirPreguntas(1, "Lenguaje", null, false, {}, {}) }
        }
        compose.onNodeWithText("El curso ya no está disponible. Vuelve a elegirlo.").assertIsDisplayed()
        compose.onNodeWithText("Confirmar y compartir").assertIsNotEnabled()
    }
}
