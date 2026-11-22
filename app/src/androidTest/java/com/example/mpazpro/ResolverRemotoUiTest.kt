package com.example.mpazpro

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mpazpro.data.local.IntentoActividadEntity
import com.example.mpazpro.model.ActividadParaResolver
import com.example.mpazpro.model.ResolverActividadUiState
import com.example.mpazpro.ui.screens.ResolverActividadContenido
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ResolverRemotoUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()

    private val actividad = ActividadParaResolver(
        7, "matematica", "Sumemos", "Lee con calma", "¿Cuánto es 2 + 2?",
        "Cuatro", "Tres", "Cinco", "Seis", maxIntentos = 3, pista = "Piensa en dos grupos."
    )

    @Test fun permiteResponderSinMostrarProgresoLocalComoSiIncluyeraPreguntasCompartidas() {
        val estado = mutableStateOf(ResolverActividadUiState(
            actividad = actividad, cargando = false, cargandoContinuacion = false
        ))
        compose.setContent {
            ResolverActividadContenido(
                estado.value,
                onVolver = {},
                onSeleccionar = { estado.value = estado.value.copy(respuestaSeleccionada = it) },
                onEnviar = {
                    val elegida = estado.value.respuestaSeleccionada
                    estado.value = estado.value.copy(
                        intento = IntentoActividadEntity(estudianteId = 1, actividadId = 7,
                            respuestaElegida = elegida, respuestaCorrecta = 0, correcta = elegida == 0),
                        explicacion = "Dos más dos son cuatro."
                    )
                },
                onReintentar = {},
                mostrarProgreso = false,
                mostrarEstrella = false
            )
        }
        compose.onNodeWithText("Tu avance en esta asignatura").assertDoesNotExist()
        compose.onNodeWithText("A. Cuatro").performScrollTo().performClick()
        compose.onNodeWithText("Enviar respuesta").performScrollTo().performClick()
        compose.onNodeWithText("¡Lo lograste! ¡Muy bien! ⭐").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Dos más dos son cuatro.").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, estado.value.intento!!.respuestaElegida) }
    }

    @Test fun respuestaPendienteNoPermiteCambiarLaSeleccionPeroSiReintentarEnvio() {
        val estado = ResolverActividadUiState(
            actividad = actividad, cargando = false, respuestaSeleccionada = 1,
            seleccionBloqueada = true, mensaje = "Intenta enviar la misma otra vez."
        )
        var envios = 0
        compose.setContent {
            ResolverActividadContenido(estado, onVolver = {}, onSeleccionar = {}, onEnviar = { envios++ },
                onReintentar = {}, mostrarProgreso = false, mostrarEstrella = false)
        }
        compose.onNodeWithText("A. Cuatro").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Enviar respuesta").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, envios) }
    }
}
