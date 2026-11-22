package com.example.mpazpro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mpazpro.ui.screens.CursoPropuesto
import com.example.mpazpro.ui.screens.TarjetaCursoPropuesto
import com.example.mpazpro.ui.screens.CursoEnLista
import com.example.mpazpro.data.local.CursoEntity
import com.example.mpazpro.model.ConexionCurso
import com.example.mpazpro.ui.theme.DocenteTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class VinculoCursoUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()

    @Test fun muestraUnSoloCursoYSuEstadoDeConexion() {
        var aperturas = 0
        compose.setContent {
            DocenteTheme {
                CursoEnLista(CursoEntity(id = 1, docenteId = 3, nombre = "3A"), ConexionCurso.CONECTADO, true) { aperturas++ }
            }
        }
        compose.onNodeWithText("3.º Básico A").assertIsDisplayed()
        compose.onNodeWithText("Conectado").assertIsDisplayed()
        compose.onNodeWithText("Vincular curso").assertDoesNotExist()
        compose.onNodeWithText("3.º Básico A").performClick()
        compose.runOnIdle { assertEquals(1, aperturas) }
    }

    @Test fun catalogoTemporalNoOfreceVincular() {
        compose.setContent {
            DocenteTheme { TarjetaCursoPropuesto(CursoPropuesto(1, 1, "3 A", "temporal"), false) {} }
        }
        compose.onNodeWithText("No disponible con un catálogo temporal.").assertIsDisplayed()
        compose.onNodeWithText("Vincular curso").assertDoesNotExist()
    }

    @Test fun vinculacionDisponibleSoloOcurreAlPresionar() {
        var pulsaciones = 0
        compose.setContent {
            DocenteTheme { TarjetaCursoPropuesto(CursoPropuesto(1, 1, "3 A", "disponible"), false) { pulsaciones++ } }
        }
        assertEquals(0, pulsaciones)
        compose.onNodeWithText("Vincular curso").performClick()
        compose.runOnIdle { assertEquals(1, pulsaciones) }
    }
}
