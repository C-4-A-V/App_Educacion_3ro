package com.example.mpazpro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
        compose.onNodeWithText("Sincronizado").assertIsDisplayed()
        compose.onNodeWithText("Vincular curso").assertDoesNotExist()
        compose.onNodeWithText("3.º Básico A").performClick()
        compose.runOnIdle { assertEquals(1, aperturas) }
    }

    @Test fun muestraEstadoSinConexion() {
        compose.setContent {
            DocenteTheme {
                CursoEnLista(CursoEntity(id = 1, docenteId = 3, nombre = "3A"), ConexionCurso.SIN_CONEXION, true) {}
            }
        }
        compose.onNodeWithText("Guardado · sin conexión").assertIsDisplayed()
    }

    @Test fun muestraEstadoRevisarConexion() {
        compose.setContent {
            DocenteTheme {
                CursoEnLista(CursoEntity(id = 1, docenteId = 3, nombre = "3A"), ConexionCurso.REVISAR, true) {}
            }
        }
        compose.onNodeWithText("Guardado en este dispositivo").assertIsDisplayed()
    }
}
