package com.example.mpazpro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mpazpro.ui.screens.BotonActualizarCursos
import com.example.mpazpro.ui.theme.DocenteTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ActualizarCursosUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()

    @Test fun botonDisparaActualizacionSoloAlPresionar() {
        var pulsaciones = 0
        compose.setContent { DocenteTheme { BotonActualizarCursos(false, false) { pulsaciones++ } } }
        compose.runOnIdle { assertEquals(0, pulsaciones) }
        compose.onNodeWithContentDescription("Actualizar cursos").assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(1, pulsaciones) }
    }

    @Test fun botonNoPermiteOtraPulsacionMientrasActualiza() {
        compose.setContent { DocenteTheme { BotonActualizarCursos(false, true) {} } }
        compose.onNodeWithContentDescription("Actualizando cursos").assertExists()
        compose.onNodeWithContentDescription("Actualizando cursos").assertIsNotEnabled()
    }
}
