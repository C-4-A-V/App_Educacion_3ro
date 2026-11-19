package com.example.mpazpro

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.mpazpro.ui.theme.*
import com.example.mpazpro.data.repository.PaletaEstudiante
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class ContrasteTemasTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()
    private fun contraste(texto: Color, fondo: Color): Float {
        val a = texto.luminance()
        val b = fondo.luminance()
        return (max(a, b) + .05f) / (min(a, b) + .05f)
    }

    private fun comprobar(pares: List<Triple<String, Color, Color>>) {
        pares.forEach { (nombre, texto, fondo) ->
            assertTrue("$nombre pierde contraste: ${contraste(texto, fondo)}",
                contraste(texto, fondo) >= 4.5f)
        }
    }

    @Composable
    private fun paresTema(): List<Triple<String, Color, Color>> {
        val c = MaterialTheme.colorScheme
        return listOf(
            Triple("Texto heredado sobre fondo", LocalContentColor.current, c.background),
            Triple("Texto de superficie", c.onSurface, c.surface),
            Triple("Texto secundario", c.onSurfaceVariant, c.surfaceContainerLow),
            Triple("Botón principal", c.onPrimary, c.primary),
            Triple("Botón secundario", c.onSecondary, c.secondary),
            Triple("Botón terciario", c.onTertiary, c.tertiary),
            Triple("Tarjeta principal", c.onPrimaryContainer, c.primaryContainer),
            Triple("Tarjeta secundaria", c.onSecondaryContainer, c.secondaryContainer),
            Triple("Tarjeta terciaria", c.onTertiaryContainer, c.tertiaryContainer),
            Triple("Enlace de tarjeta", c.primary, c.surfaceContainerLow),
            Triple("Texto de error", c.error, c.surface)
        )
    }

    @Test fun estudianteActualizaTextoAunqueElTemaPadreSeaOpuesto() {
        val oscuro = mutableStateOf(false)
        var pares = emptyList<Triple<String, Color, Color>>()
        compose.setContent {
            MPAZPROTheme(darkTheme = !oscuro.value) {
                CompositionLocalProvider(
                    LocalModoOscuroEstudiante provides oscuro.value,
                    LocalContentColor provides Color.Magenta
                ) {
                    AventuraTheme {
                        val actuales = paresTema() + (0..3).map {
                            val (fondo, texto) = colorTarjetaAventura(it)
                            Triple("Asignatura $it", texto, fondo)
                        }
                        SideEffect { pares = actuales }
                    }
                }
            }
        }
        compose.runOnIdle {
            comprobar(pares)
            oscuro.value = true
        }
        compose.runOnIdle { comprobar(pares) }
    }

    @Test fun paletasEstudianteMantienenContrasteEnModoClaroYOscuro() {
        val oscuro = mutableStateOf(false)
        val paleta = mutableStateOf(PaletaEstudiante.AVENTURA)
        var pares = emptyList<Triple<String, Color, Color>>()
        compose.setContent {
            CompositionLocalProvider(
                LocalModoOscuroEstudiante provides oscuro.value,
                LocalPaletaEstudiante provides paleta.value
            ) {
                AventuraTheme {
                    val actuales = paresTema() + (0..3).map {
                        val (fondo, texto) = colorTarjetaAventura(it)
                        Triple("Tarjeta de asignatura $it", texto, fondo)
                    }
                    SideEffect { pares = actuales }
                }
            }
        }
        for (opcion in PaletaEstudiante.entries) {
            for (modo in listOf(false, true)) {
                compose.runOnIdle { paleta.value = opcion; oscuro.value = modo }
                compose.runOnIdle { comprobar(pares) }
            }
        }
    }

    @Test fun paletasDocenteMantienenContrasteEnAmbosModos() {
        val oscuro = mutableStateOf(false)
        val paleta = mutableStateOf(PaletaEstudiante.AVENTURA)
        var pares = emptyList<Triple<String, Color, Color>>()
        compose.setContent {
            CompositionLocalProvider(
                LocalModoOscuroDocente provides oscuro.value,
                LocalPaletaDocente provides paleta.value
            ) {
                DocenteTheme {
                    val actuales = paresTema()
                    SideEffect { pares = actuales }
                }
            }
        }
        for (opcion in PaletaEstudiante.entries) {
            for (modo in listOf(false, true)) {
                compose.runOnIdle { paleta.value = opcion; oscuro.value = modo }
                compose.runOnIdle { comprobar(pares) }
            }
        }
    }

    @Test fun docenteActualizaTextoEnAmbasApariencias() {
        val oscuro = mutableStateOf(false)
        var pares = emptyList<Triple<String, Color, Color>>()
        compose.setContent {
            MPAZPROTheme(darkTheme = !oscuro.value) {
                CompositionLocalProvider(
                    LocalModoOscuroDocente provides oscuro.value,
                    LocalContentColor provides Color.Magenta
                ) {
                    DocenteTheme {
                        val actuales = paresTema()
                        SideEffect { pares = actuales }
                    }
                }
            }
        }
        compose.runOnIdle {
            comprobar(pares)
            oscuro.value = true
        }
        compose.runOnIdle { comprobar(pares) }
    }
}
