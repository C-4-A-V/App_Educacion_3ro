package com.example.mpazpro

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.mpazpro.data.local.IntentoActividadEntity
import com.example.mpazpro.model.*
import com.example.mpazpro.ui.screens.InicioEstudianteScreen
import com.example.mpazpro.ui.screens.ResolverActividadContenido
import com.example.mpazpro.ui.theme.MPAZPROTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AventuraUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()

    private fun actividad() = ActividadParaResolver(
        1, "matematica", "Sumemos", "Lee con calma", "¿Cuánto es 2 + 2?",
        "Cuatro", "Tres", "Cinco", "Seis", 3
    )

    @Test fun saludoPersonalizadoYAsignaturasAccesiblesConTextoGrande() {
        var asignatura = ""
        compose.setContent {
            MPAZPROTheme(darkTheme = true) {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.5f)) {
                    Box(Modifier.width(320.dp)) {
                        InicioEstudianteScreen(
                            PerfilEstudiante(1, "Sofía", "3.º A", null),
                            ProgresoEstudiante(4, 1, 1),
                            onSalir = {},
                            onAbrirAsignatura = { asignatura = it }
                        )
                    }
                }
            }
        }
        compose.onNodeWithText("¡Hola, Sofía!").assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Historia, Geografía y Ciencias Sociales"))
        compose.onNodeWithText("Historia, Geografía y Ciencias Sociales").performClick()
        compose.runOnIdle { assertEquals("historia", asignatura) }
    }

    @Test fun noPermiteEnviarSinSeleccion() {
        compose.setContent {
            MPAZPROTheme {
                ResolverActividadContenido(
                    ResolverActividadUiState(actividad = actividad(), cargando = false),
                    onVolver = {}, onSeleccionar = {}, onEnviar = {}, onReintentar = {}
                )
            }
        }
        compose.onNodeWithText("Enviar respuesta").performScrollTo().assertIsNotEnabled()
    }

    @Test fun errorIntermedioOfreceReintentoSinRevelarExplicacion() {
        compose.setContent {
            MPAZPROTheme {
                ResolverActividadContenido(
                    ResolverActividadUiState(
                        actividad = actividad(),
                        intento = IntentoActividadEntity(
                            estudianteId = 1, actividadId = 1, respuestaElegida = 1,
                            respuestaCorrecta = 0, correcta = false, numero = 1, finalizada = false
                        ),
                        respuestaSeleccionada = 1,
                        explicacion = "EXPLICACION_RESERVADA",
                        cargando = false
                    ),
                    onVolver = {}, onSeleccionar = {}, onEnviar = {}, onReintentar = {}
                )
            }
        }
        compose.onNodeWithText("Intentar otra vez").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("EXPLICACION_RESERVADA").assertDoesNotExist()
        compose.onNodeWithText("Respuesta correcta:", substring = true).assertDoesNotExist()
    }

    @Test fun resultadoFinalMuestraExplicacion() {
        compose.setContent {
            MPAZPROTheme {
                ResolverActividadContenido(
                    ResolverActividadUiState(
                        actividad = actividad(),
                        intento = IntentoActividadEntity(
                            estudianteId = 1, actividadId = 1, respuestaElegida = 0,
                            respuestaCorrecta = 0, correcta = true, numero = 2, finalizada = true
                        ),
                        respuestaSeleccionada = 0,
                        explicacion = "Dos más dos son cuatro.",
                        cargando = false
                    ),
                    onVolver = {}, onSeleccionar = {}, onEnviar = {}, onReintentar = {}
                )
            }
        }
        compose.onNodeWithText("Dos más dos son cuatro.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Intentar otra vez").assertDoesNotExist()
    }
}
