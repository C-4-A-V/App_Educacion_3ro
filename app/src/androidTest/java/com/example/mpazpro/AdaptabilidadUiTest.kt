package com.example.mpazpro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.mpazpro.model.PerfilEstudiante
import com.example.mpazpro.model.ProgresoEstudiante
import com.example.mpazpro.ui.components.ContenidoAdaptativo
import com.example.mpazpro.ui.screens.InicioEstudianteScreen
import com.example.mpazpro.ui.theme.MPAZPROTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class AdaptabilidadUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()

    private fun comprobar(ancho: Int, columnas: Int) {
        compose.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(ancho.dp, 1000.dp))) {
                MPAZPROTheme {
                    ContenidoAdaptativo {
                        InicioEstudianteScreen(
                            PerfilEstudiante(1, "Sofía", "3.º A", null),
                            ProgresoEstudiante(4, 1, 1), {}, {}
                        )
                    }
                }
            }
        }
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(4)
        val lenguaje = compose.onNodeWithText("Lenguaje y Comunicación").getUnclippedBoundsInRoot()
        val matematica = compose.onNodeWithText("Matemática").getUnclippedBoundsInRoot()
        if (columnas == 1) {
            assertEquals(lenguaje.left.value, matematica.left.value, 1f)
            assertTrue(matematica.top > lenguaje.top)
        } else {
            assertTrue(matematica.left > lenguaje.left)
            assertEquals(lenguaje.top.value, matematica.top.value, 1f)
            if (columnas == 3) {
                val ciencias = compose.onNodeWithText("Ciencias Naturales").getUnclippedBoundsInRoot()
                assertTrue(ciencias.left > matematica.left)
                assertEquals(matematica.top.value, ciencias.top.value, 1f)
            }
        }
    }

    @Test fun compactoUsaUnaColumna() = comprobar(400, 1)
    @Test fun medianoUsaDosColumnas() = comprobar(610, 2)
    @Test fun expandidoUsaTresColumnas() = comprobar(900, 3)
}
