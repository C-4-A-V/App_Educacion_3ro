package com.example.mpazpro

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mpazpro.model.ActividadParaResolver
import com.example.mpazpro.model.ProgresoEstudiante
import com.example.mpazpro.model.ResolverActividadUiState
import com.example.mpazpro.ui.screens.ResolverActividadContenido
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AyudaEstudianteUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()
    private val actividad = ActividadParaResolver(
        1, "matematica", "Sumemos", "Lee con calma", "¿Cuánto es 2 + 2?",
        "Cuatro", "Tres", "Cinco", "Seis", maxIntentos = 3, pista = "Dibuja dos grupos de dos."
    )

    @Test fun pistaSeAbreYCierraSinCambiarLaSeleccion() {
        val estado = mutableStateOf(ResolverActividadUiState(
            actividad = actividad, cargando = false, progresoAsignatura = ProgresoEstudiante(10)
        ))
        compose.setContent {
            ResolverActividadContenido(
                estado.value, onVolver = {}, onSeleccionar = { indice ->
                    estado.value = estado.value.copy(respuestaSeleccionada = indice)
                }, onEnviar = {}, onReintentar = {}
            )
        }
        compose.onNodeWithText("Dibuja dos grupos de dos.").assertDoesNotExist()
        compose.onNodeWithText("B. Tres").performScrollTo().performClick()
        compose.onNodeWithText("¿Necesitas una pista?").performScrollTo().performClick()
        compose.onNodeWithText("Dibuja dos grupos de dos.").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, estado.value.respuestaSeleccionada) }
        compose.onNodeWithText("Ocultar pista").performScrollTo().performClick()
        compose.onNodeWithText("Dibuja dos grupos de dos.").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, estado.value.respuestaSeleccionada) }
    }

    @Test fun evaluacionNoMuestraAyudasAunqueElModeloContengaPista() {
        compose.setContent {
            ResolverActividadContenido(
                ResolverActividadUiState(actividad = actividad.copy(maxIntentos = 1), cargando = false),
                onVolver = {}, onSeleccionar = {}, onEnviar = {}, onReintentar = {}
            )
        }
        compose.onNodeWithText("¿Necesitas una pista?").assertDoesNotExist()
        compose.onNodeWithText("Dibuja dos grupos de dos.").assertDoesNotExist()
    }

    @Test fun celebracionSoloApareceAlCompletarLaAsignatura() {
        val estado = mutableStateOf(ResolverActividadUiState(
            actividad = actividad, cargando = false, progresoAsignatura = ProgresoEstudiante(10, 9, 4)
        ))
        compose.setContent {
            ResolverActividadContenido(
                estado.value, onVolver = {}, onSeleccionar = {}, onEnviar = {}, onReintentar = {}
            )
        }
        compose.waitForIdle()
        compose.onNodeWithText("¡Una aventura completada!").assertDoesNotExist()
        compose.runOnIdle {
            estado.value = estado.value.copy(progresoAsignatura = ProgresoEstudiante(10, 10, 5))
        }
        compose.onNodeWithText("¡Una aventura completada!").performScrollTo().assertIsDisplayed()
    }
}
