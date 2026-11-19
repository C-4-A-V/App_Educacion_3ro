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
import com.example.mpazpro.data.repository.PaletaEstudiante

val LocalModoOscuroEstudiante = staticCompositionLocalOf<Boolean?> { null }
val LocalPaletaEstudiante = staticCompositionLocalOf { PaletaEstudiante.AVENTURA }

internal fun coloresPaleta(paleta: PaletaEstudiante, oscuro: Boolean): ColorScheme {
    val base = if (oscuro) darkColorScheme(
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
    return when (paleta) {
        PaletaEstudiante.AVENTURA -> if (oscuro) base.copy(
            primary = Color(0xFFDAB6FF), onPrimary = Color(0xFF400B70),
            primaryContainer = Color(0xFF5F258C), onPrimaryContainer = Color(0xFFF6DEFF),
            secondary = Color(0xFFFFACD1), onSecondary = Color(0xFF570D36),
            secondaryContainer = Color(0xFF80315E), onSecondaryContainer = Color(0xFFFFD9E9),
            tertiary = Color(0xFFFFB3D2), onTertiary = Color(0xFF5D123C),
            tertiaryContainer = Color(0xFF75305B), onTertiaryContainer = Color(0xFFFFD9E9),
            background = Color(0xFF301D40), onBackground = Color(0xFFF9E9FF),
            surface = Color(0xFF301D40), onSurface = Color(0xFFF9E9FF),
            surfaceVariant = Color(0xFF513A5A), onSurfaceVariant = Color(0xFFF0DEF1),
            surfaceContainerLow = Color(0xFF3D274B)
        ) else base.copy(
            primary = Color(0xFF712BB3), onPrimary = Color.White,
            primaryContainer = Color(0xFFEBD4FF), onPrimaryContainer = Color(0xFF390365),
            secondary = Color(0xFFAE256A), onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFD8E9), onSecondaryContainer = Color(0xFF50092F),
            tertiary = Color(0xFFA3266A), onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFD6E9), onTertiaryContainer = Color(0xFF500A33),
            background = Color(0xFFFFF2FE), onBackground = Color(0xFF3B2142),
            surface = Color(0xFFFFF8FF), onSurface = Color(0xFF3B2142),
            surfaceVariant = Color(0xFFF5E1F4), onSurfaceVariant = Color(0xFF5C405E),
            surfaceContainerLow = Color(0xFFFFF5FF)
        )
        PaletaEstudiante.OCEANO -> if (oscuro) base.copy(
            primary = Color(0xFFA9D4FF), onPrimary = Color(0xFF003354),
            primaryContainer = Color(0xFF154D72), onPrimaryContainer = Color(0xFFD7ECFF),
            secondary = Color(0xFFA9EC9B), onSecondary = Color(0xFF123C14),
            secondaryContainer = Color(0xFF245C28), onSecondaryContainer = Color(0xFFD0FFC3),
            tertiary = Color(0xFFA9EC9B), onTertiary = Color(0xFF123C14),
            tertiaryContainer = Color(0xFF245C28), onTertiaryContainer = Color(0xFFD0FFC3),
            background = Color(0xFF112C39), onBackground = Color(0xFFE3F2FA),
            surface = Color(0xFF112C39), onSurface = Color(0xFFE3F2FA),
            surfaceVariant = Color(0xFF304A56), onSurfaceVariant = Color(0xFFD5E6EE),
            surfaceContainerLow = Color(0xFF1C3642)
        ) else base.copy(
            primary = Color(0xFF005F91), onPrimary = Color.White,
            primaryContainer = Color(0xFFCDE6FF), onPrimaryContainer = Color(0xFF003456),
            secondary = Color(0xFF276B2B), onSecondary = Color.White,
            secondaryContainer = Color(0xFFC0F2B4), onSecondaryContainer = Color(0xFF103E13),
            tertiary = Color(0xFF276B2B), onTertiary = Color.White,
            tertiaryContainer = Color(0xFFC0F2B4), onTertiaryContainer = Color(0xFF103E13),
            background = Color(0xFFEAF8FF), onBackground = Color(0xFF123444),
            surface = Color(0xFFF4FBFF), onSurface = Color(0xFF123444),
            surfaceVariant = Color(0xFFDCEEF7), onSurfaceVariant = Color(0xFF344F5C),
            surfaceContainerLow = Color(0xFFF1F9FF)
        )
        PaletaEstudiante.ATARDECER -> if (oscuro) base.copy(
            primary = Color(0xFFFFB4AB), onPrimary = Color(0xFF69000B),
            primaryContainer = Color(0xFF8A1C28), onPrimaryContainer = Color(0xFFFFDAD6),
            secondary = Color(0xFFFFDA72), onSecondary = Color(0xFF443000),
            secondaryContainer = Color(0xFF604600), onSecondaryContainer = Color(0xFFFFE8A5),
            tertiary = Color(0xFFFFD58E), onTertiary = Color(0xFF452B00),
            tertiaryContainer = Color(0xFF604515), onTertiaryContainer = Color(0xFFFFEAC8),
            background = Color(0xFF36251F), onBackground = Color(0xFFFFEDE5),
            surface = Color(0xFF36251F), onSurface = Color(0xFFFFEDE5),
            surfaceVariant = Color(0xFF523F39), onSurfaceVariant = Color(0xFFF4DED6),
            surfaceContainerLow = Color(0xFF412E28)
        ) else base.copy(
            primary = Color(0xFFB51D32), onPrimary = Color.White,
            primaryContainer = Color(0xFFFFD6D4), onPrimaryContainer = Color(0xFF57000A),
            secondary = Color(0xFF755500), onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFE688), onSecondaryContainer = Color(0xFF3F2D00),
            tertiary = Color(0xFF725500), onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFE6A4), onTertiaryContainer = Color(0xFF483500),
            background = Color(0xFFFFF4ED), onBackground = Color(0xFF493027),
            surface = Color(0xFFFFF9F5), onSurface = Color(0xFF493027),
            surfaceVariant = Color(0xFFF7E5DC), onSurfaceVariant = Color(0xFF5C443C),
            surfaceContainerLow = Color(0xFFFFF5EF)
        )
    }
}

@Composable
fun AventuraTheme(content: @Composable () -> Unit) {
    val oscuro = LocalModoOscuroEstudiante.current ?: (MaterialTheme.colorScheme.background.luminance() < .5f)
    val colores = coloresPaleta(LocalPaletaEstudiante.current, oscuro)
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
    val colores = MaterialTheme.colorScheme
    val tarjetas = listOf(
        colores.primaryContainer to colores.onPrimaryContainer,
        colores.secondaryContainer to colores.onSecondaryContainer,
        colores.tertiaryContainer to colores.onTertiaryContainer,
        colores.surfaceVariant to colores.onSurfaceVariant
    )
    return tarjetas[Math.floorMod(indice, tarjetas.size)]
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
