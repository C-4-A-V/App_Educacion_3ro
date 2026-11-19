package com.example.mpazpro

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mpazpro.model.*
import com.example.mpazpro.ui.screens.InicioEstudianteScreen
import com.example.mpazpro.ui.screens.PerfilEstudianteScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PerfilEstudianteUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()
    private val perfil = PerfilEstudiante(1, "Sofía", "3.º Básico A", null)

    @Test fun accesoAlPerfilEstaJuntoAlSaludo() {
        var aperturas = 0
        compose.setContent {
            InicioEstudianteScreen(perfil, ProgresoEstudiante(), onSalir = {},
                onAbrirAsignatura = {}, onAbrirPerfil = { aperturas++ })
        }
        compose.onNodeWithText("¡Hola, Sofía!").assertIsDisplayed()
        compose.onNodeWithText("Mi perfil").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, aperturas) }
    }

    @Test fun perfilPermiteCambiarAjustesObjetivoYContinuar() {
        var continuaciones = 0
        val estado = mutableStateOf(AccesoEstudianteUiState(
            seleccionado = perfil, progreso = ProgresoEstudiante(10, 3, 2),
            preferenciasCargadas = true, cargando = false,
            preferencias = com.example.mpazpro.data.repository.PreferenciasPerfilEstudiante(modoOscuro = false)
        ))
        compose.setContent {
            PerfilEstudianteScreen(
                estado = estado.value, onVolver = {}, onContinuar = { continuaciones++ },
                onOscuro = { estado.value = estado.value.copy(preferencias = estado.value.preferencias.copy(modoOscuro = it)) },
                onPaleta = { estado.value = estado.value.copy(preferencias = estado.value.preferencias.copy(paleta = it)) },
                onTextoGrande = { estado.value = estado.value.copy(preferencias = estado.value.preferencias.copy(textoGrande = it)) },
                onObjetivo = { estado.value = estado.value.copy(preferencias = estado.value.preferencias.copy(objetivo = it)) },
                onFoto = {}, onErrorFoto = {}
            )
        }
        compose.onNodeWithText("Sofía").assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("30% de mi aventura completada"))
        compose.onNodeWithText("30% de mi aventura completada").assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Océano"))
        compose.onNodeWithText("Océano").performClick()
        compose.runOnIdle {
            assertEquals(com.example.mpazpro.data.repository.PaletaEstudiante.OCEANO, estado.value.preferencias.paleta)
        }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasContentDescription("Modo oscuro"))
        compose.onNodeWithContentDescription("Modo oscuro").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Modo oscuro").assertIsOn()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasContentDescription("Texto más grande"))
        compose.onNodeWithContentDescription("Texto más grande").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Texto más grande").assertIsOn()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Guardar mi objetivo"))
        compose.onNodeWithText("Guardar mi objetivo").assertIsNotEnabled()
        compose.onNodeWithText("Escribe tu objetivo").performScrollTo().performTextInput("Leer un cuento con mi familia")
        compose.onNodeWithText("Guardar mi objetivo").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("Leer un cuento con mi familia", estado.value.preferencias.objetivo) }
        compose.onNodeWithText("Guardar mi objetivo").assertIsNotEnabled()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Continuar mi aventura →"))
        compose.onNodeWithText("Continuar mi aventura →").performClick()
        compose.runOnIdle { assertEquals(1, continuaciones) }
    }
}
