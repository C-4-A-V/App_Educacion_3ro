package com.example.mpazpro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.mpazpro.model.Asignatura

/** Tema acotado al área infantil; el panel docente conserva su identidad. */
@Composable
fun AventuraTheme(content: @Composable () -> Unit) {
    val oscuro = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val colores = if (oscuro) darkColorScheme(
        primary = Color(0xFFD2BCFF), onPrimary = Color(0xFF38245C),
        primaryContainer = Color(0xFF4D386F), onPrimaryContainer = Color(0xFFF0E3FF),
        secondary = Color(0xFFA9CDFF), onSecondary = Color(0xFF12365C),
        secondaryContainer = Color(0xFF294B73), onSecondaryContainer = Color(0xFFD9E9FF),
        tertiary = Color(0xFFABDABD), onTertiary = Color(0xFF123C29),
        tertiaryContainer = Color(0xFF29553E), onTertiaryContainer = Color(0xFFD4F4DE),
        background = Color(0xFF1D1B20), onBackground = Color(0xFFF4ECE4),
        surface = Color(0xFF1D1B20), onSurface = Color(0xFFF4ECE4)
    ) else lightColorScheme(
        primary = Color(0xFF684397), onPrimary = Color.White,
        primaryContainer = Color(0xFFEEDFFF), onPrimaryContainer = Color(0xFF35204F),
        secondary = Color(0xFF285C96), onSecondary = Color.White,
        secondaryContainer = Color(0xFFDDEBFF), onSecondaryContainer = Color(0xFF183857),
        tertiary = Color(0xFF286345), onTertiary = Color.White,
        tertiaryContainer = Color(0xFFDEF1DE), onTertiaryContainer = Color(0xFF1D4530),
        background = Color(0xFFFFF9EE), onBackground = Color(0xFF302B36),
        surface = Color(0xFFFFF9EE), onSurface = Color(0xFF302B36),
        surfaceVariant = Color(0xFFF1E9DF), onSurfaceVariant = Color(0xFF57505B)
    )
    MaterialTheme(colorScheme = colores, typography = MaterialTheme.typography, content = content)
}

@Composable
fun Asignatura.colorAventura(): Color = when (this) {
    Asignatura.LENGUAJE -> MaterialTheme.colorScheme.primaryContainer
    Asignatura.MATEMATICA -> MaterialTheme.colorScheme.secondaryContainer
    Asignatura.CIENCIAS -> MaterialTheme.colorScheme.tertiaryContainer
    Asignatura.HISTORIA -> if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF65412A) else Color(0xFFFFE3C9)
}

@Composable
fun Asignatura.textoAventura(): Color = when (this) {
    Asignatura.LENGUAJE -> MaterialTheme.colorScheme.onPrimaryContainer
    Asignatura.MATEMATICA -> MaterialTheme.colorScheme.onSecondaryContainer
    Asignatura.CIENCIAS -> MaterialTheme.colorScheme.onTertiaryContainer
    Asignatura.HISTORIA -> if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFFFE3C9) else Color(0xFF603516)
}

val Asignatura.iconoAventura: String
    get() = when (this) {
        Asignatura.LENGUAJE -> "📖"
        Asignatura.MATEMATICA -> "🔢"
        Asignatura.CIENCIAS -> "🌱"
        Asignatura.HISTORIA -> "🌎"
    }
