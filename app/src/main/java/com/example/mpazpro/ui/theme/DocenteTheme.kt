package com.example.mpazpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val DocenteClaro = lightColorScheme(
    surfaceTint = Color.Transparent,
    primary = Color(0xFF236B59), onPrimary = Color.White,
    primaryContainer = Color(0xFFB9DCCB), onPrimaryContainer = Color(0xFF173F33),
    secondary = Color(0xFF435F8C), onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7D6EE), onSecondaryContainer = Color(0xFF213B61),
    tertiary = Color(0xFF8B5B2B), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEBCB9C), onTertiaryContainer = Color(0xFF513514),
    background = Color(0xFFF7F6F0), onBackground = Color(0xFF243833),
    surface = Color(0xFFFFFEFA), onSurface = Color(0xFF243833),
    surfaceVariant = Color(0xFFDEE6D9), onSurfaceVariant = Color(0xFF52605A),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF2F3ED),
    surfaceContainer = Color(0xFFECEFE8),
    surfaceContainerHigh = Color(0xFFE6EAE2),
    surfaceContainerHighest = Color(0xFFE0E5DC),
    outline = Color(0xFF75847B), outlineVariant = Color(0xFFB5C4B4),
    error = Color(0xFFAD3434), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002)
)
private val DocenteOscuro = darkColorScheme(
    primary = Color(0xFF91D4B5), onPrimary = Color(0xFF073A32),
    primaryContainer = Color(0xFF285D49), onPrimaryContainer = Color(0xFFD2F2DF),
    secondary = Color(0xFFB0C8F1), onSecondary = Color(0xFF233448),
    secondaryContainer = Color(0xFF354F77), onSecondaryContainer = Color(0xFFD9E5F7),
    tertiary = Color(0xFFEFCA95), onTertiary = Color(0xFF492A17),
    tertiaryContainer = Color(0xFF6A4D27), onTertiaryContainer = Color(0xFFFFDCC5),
    background = Color(0xFF141C19), onBackground = Color(0xFFE0E8DF),
    surface = Color(0xFF19221E), onSurface = Color(0xFFE0E8DF),
    surfaceVariant = Color(0xFF39483F), onSurfaceVariant = Color(0xFFC2CFC4),
    surfaceContainerLowest = Color(0xFF101713),
    surfaceContainerLow = Color(0xFF1D2721),
    surfaceContainer = Color(0xFF222D26),
    surfaceContainerHigh = Color(0xFF2D3831),
    surfaceContainerHighest = Color(0xFF38443B),
    outline = Color(0xFF8C9C90), outlineVariant = Color(0xFF435248)
)

val LocalModoOscuroDocente = staticCompositionLocalOf<Boolean?> { null }

/** Identidad exclusiva del área docente; conserva la tipografía y lectura cómoda de la app. */
@Composable
fun DocenteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (LocalModoOscuroDocente.current ?: isSystemInDarkTheme()) DocenteOscuro else DocenteClaro,
        typography = MaterialTheme.typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(24.dp)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

@Composable
fun EncabezadoDocente(titulo: String, detalle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "MPAZ PRO · ESPACIO DOCENTE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(titulo, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text(detalle, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
