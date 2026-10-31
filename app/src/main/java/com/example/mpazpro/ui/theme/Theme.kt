package com.example.mpazpro.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val ColoresClaros = lightColorScheme(
    primary = Color(0xFF4F609D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE3FF),
    onPrimaryContainer = Color(0xFF25335D),

    secondary = Color(0xFF69548C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEDFFF),
    onSecondaryContainer = Color(0xFF3D285C),

    tertiary = Color(0xFF35695B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD6F0E5),
    onTertiaryContainer = Color(0xFF164C3E),

    background = Color(0xFFF8F9FE),
    onBackground = Color(0xFF252B3D),

    surface = Color(0xFFF8F9FE),
    onSurface = Color(0xFF252B3D),
    surfaceVariant = Color(0xFFE8EAF2),
    onSurfaceVariant = Color(0xFF50586B),

    outline = Color(0xFF747C90),
    outlineVariant = Color(0xFFD9DEEB),

    error = Color(0xFF9D3643),
    onError = Color.White,
    errorContainer = Color(0xFFFFDFE2),
    onErrorContainer = Color(0xFF641D2A)
)

private val ColoresOscuros = darkColorScheme(
    primary = Color(0xFFB9C5FF),
    onPrimary = Color(0xFF21305F),
    primaryContainer = Color(0xFF394873),
    onPrimaryContainer = Color(0xFFDCE3FF),

    secondary = Color(0xFFD7BEF6),
    onSecondary = Color(0xFF39234F),
    secondaryContainer = Color(0xFF514067),
    onSecondaryContainer = Color(0xFFEEDFFF),

    tertiary = Color(0xFFA6D5C3),
    onTertiary = Color(0xFF0B382B),
    tertiaryContainer = Color(0xFF255044),
    onTertiaryContainer = Color(0xFFD6F0E5),

    background = Color(0xFF131620),
    onBackground = Color(0xFFE5E7F1),

    surface = Color(0xFF131620),
    onSurface = Color(0xFFE5E7F1),
    surfaceVariant = Color(0xFF303645),
    onSurfaceVariant = Color(0xFFC3C8D8),

    outline = Color(0xFF929AAD),
    outlineVariant = Color(0xFF424959),

    error = Color(0xFFFFB2BC),
    onError = Color(0xFF5F1223),
    errorContainer = Color(0xFF7E293A),
    onErrorContainer = Color(0xFFFFDFE2)
)

@Composable
fun MPAZPROTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colores = when {
        dynamicColor &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {

            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> ColoresOscuros

        else -> ColoresClaros
    }

    MaterialTheme(
        colorScheme = colores,
        typography = Typography,
        content = content
    )
}
