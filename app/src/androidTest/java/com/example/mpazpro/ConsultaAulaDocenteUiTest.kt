package com.example.mpazpro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mpazpro.data.remote.ActividadRemotaEstudiante
import com.example.mpazpro.ui.screens.TarjetaPreguntaSoloServidor
import com.example.mpazpro.ui.theme.DocenteTheme
import org.junit.Rule
import org.junit.Test

class ConsultaAulaDocenteUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()

    @Test fun preguntaSinCopiaSeMuestraComoConsulta() {
        val pregunta = ActividadRemotaEstudiante(7, "lenguaje", "Lectura compartida", "Lee",
            "¿Qué es un cuento?", "Historia", "Árbol", "Número", "Suma", 3, "")
        compose.setContent { DocenteTheme { TarjetaPreguntaSoloServidor(pregunta) } }
        compose.onNodeWithText("Lectura compartida").assertIsDisplayed()
        compose.onNodeWithText("¿Qué es un cuento?").assertIsDisplayed()
        compose.onNodeWithText("Compartida · sin copia en este dispositivo").assertIsDisplayed()
        compose.onNodeWithText("Compartir con otro curso").assertDoesNotExist()
    }
}
