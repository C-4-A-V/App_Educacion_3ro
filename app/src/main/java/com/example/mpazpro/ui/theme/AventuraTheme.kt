package com.example.mpazpro.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.mpazpro.model.Asignatura

val LocalModoOscuroEstudiante = staticCompositionLocalOf<Boolean?> { null }

@Composable
fun AventuraTheme(content: @Composable () -> Unit) {
    val oscuro = LocalModoOscuroEstudiante.current ?: (MaterialTheme.colorScheme.background.luminance() < .5f)
    val colores = if (oscuro) darkColorScheme(
        primary = Color(0xFFCAB5FF), onPrimary = Color(0xFF332059),
        primaryContainer = Color(0xFF4C3376), onPrimaryContainer = Color(0xFFF0E5FF),
        secondary = Color(0xFF8FDEDB), onSecondary = Color(0xFF003B3A),
        secondaryContainer = Color(0xFF125552), onSecondaryContainer = Color(0xFFC2F6ED),
        tertiary = Color(0xFFFFD578), onTertiary = Color(0xFF493200),
        tertiaryContainer = Color(0xFF654A14), onTertiaryContainer = Color(0xFFFFEAC1),
        background = Color(0xFF172A32), onBackground = Color(0xFFF1F5ED),
        surface = Color(0xFF172A32), onSurface = Color(0xFFF1F5ED),
        surfaceVariant = Color(0xFF30474D), onSurfaceVariant = Color(0xFFD2E3E2)
    ) else lightColorScheme(
        primary = Color(0xFF6540B5), onPrimary = Color.White,
        primaryContainer = Color(0xFFE9DDFF), onPrimaryContainer = Color(0xFF35205C),
        secondary = Color(0xFF087B75), onSecondary = Color.White,
        secondaryContainer = Color(0xFFC2F1E5), onSecondaryContainer = Color(0xFF104B42),
        tertiary = Color(0xFF805300), onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFE6A4), onTertiaryContainer = Color(0xFF573900),
        background = Color(0xFFF0FAFF), onBackground = Color(0xFF203644),
        surface = Color(0xFFF8FCFF), onSurface = Color(0xFF203644),
        surfaceVariant = Color(0xFFE4F0F5), onSurfaceVariant = Color(0xFF445D69),
        outline = Color(0xFF637F8B), outlineVariant = Color(0xFFC8DFE8)
    )
    val tipo = MaterialTheme.typography
    MaterialTheme(
        colorScheme = colores,
        typography = tipo.copy(
            headlineLarge = tipo.headlineLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold),
            headlineMedium = tipo.headlineMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
            titleLarge = tipo.titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
            titleMedium = tipo.titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold)
        ),
        content = {
            // MaterialTheme cambia la paleta, pero el texto sin color explícito necesita
            // recibir también el color del nuevo fondo, incluso si el tema padre es opuesto.
            CompositionLocalProvider(LocalContentColor provides colores.onBackground) {
                content()
            }
        }
    )
}

@Composable
fun colorTarjetaAventura(indice: Int): Pair<Color, Color> {
    val oscuro = MaterialTheme.colorScheme.background.luminance() < .5f
    val claros = listOf(
        Color(0xFFFFE3A0) to Color(0xFF5C3C09),
        Color(0xFFBFEDE5) to Color(0xFF124B43),
        Color(0xFFFFD8D9) to Color(0xFF692F40),
        Color(0xFFDADFFF) to Color(0xFF353F75)
    )
    val oscuros = listOf(
        Color(0xFF624B20) to Color(0xFFFFEDC0),
        Color(0xFF20574D) to Color(0xFFC7F5E6),
        Color(0xFF663B4B) to Color(0xFFFFE1E7),
        Color(0xFF3E4774) to Color(0xFFE5E7FF)
    )
    return (if (oscuro) oscuros else claros)[Math.floorMod(indice, 4)]
}

@Composable
fun Asignatura.colorAventura(): Color = colorTarjetaAventura(indiceColor).first

@Composable
fun Asignatura.textoAventura(): Color = colorTarjetaAventura(indiceColor).second

private val Asignatura.indiceColor: Int
    get() = when (this) {
        Asignatura.LENGUAJE -> 3
        Asignatura.MATEMATICA -> 0
        Asignatura.CIENCIAS -> 1
        Asignatura.HISTORIA -> 2
    }

val Asignatura.iconoAventura: String
    get() = when (this) {
        Asignatura.LENGUAJE -> "📖"
        Asignatura.MATEMATICA -> "🔢"
        Asignatura.CIENCIAS -> "🌱"
        Asignatura.HISTORIA -> "🌎"
    }
