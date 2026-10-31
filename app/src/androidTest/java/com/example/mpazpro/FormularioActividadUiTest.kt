package com.example.mpazpro

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mpazpro.data.local.CursoEntity
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.model.CrearActividadUiState
import com.example.mpazpro.ui.screens.CrearActividadContenido
import com.example.mpazpro.ui.theme.MPAZPROTheme
import com.example.mpazpro.validation.ActividadValidator
import org.junit.Rule
import org.junit.Test

class FormularioActividadUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()

    private fun mostrar(texto: String) {
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(texto))
    }

    @Test fun muestraErroresPorCampoYPermiteCorregirTitulo() {
        val estado = mutableStateOf(CrearActividadUiState(
            curso = CursoEntity(id = 1, docenteId = 1, nombre = "3.º A"), cargando = false
        ))
        compose.setContent {
            MPAZPROTheme {
                CrearActividadContenido(
                    estado = estado.value, asignatura = Asignatura.MATEMATICA, onVolver = {},
                    onTitulo = {
                        val f = estado.value.formulario.copy(titulo = it)
                        estado.value = estado.value.copy(formulario = f, errores = ActividadValidator.errores(f))
                    },
                    onInstrucciones = {}, onPregunta = {}, onExplicacion = {},
                    onAlternativa = { _, _ -> }, onCorrecta = {}, onModalidad = {},
                    onGuardar = {
                        estado.value = estado.value.copy(
                            errores = ActividadValidator.errores(estado.value.formulario), intentoValidar = true
                        )
                    }
                )
            }
        }
        mostrar("Guardar actividad")
        compose.onNodeWithText("Guardar actividad").performClick()
        mostrar("Usa entre 3 y 80 caracteres.")
        compose.onNodeWithText("Usa entre 3 y 80 caracteres.").assertIsDisplayed()
        compose.onNodeWithText("Título de la actividad").performTextInput("Una actividad válida")
        compose.onNodeWithText("Usa entre 3 y 80 caracteres.").assertDoesNotExist()
        mostrar("Escribe una explicación de hasta 1000 caracteres.")
        compose.onNodeWithText("Escribe una explicación de hasta 1000 caracteres.").assertIsDisplayed()
        mostrar("Marca la alternativa correcta.")
        compose.onNodeWithText("Marca la alternativa correcta.").assertIsDisplayed()
    }
}
