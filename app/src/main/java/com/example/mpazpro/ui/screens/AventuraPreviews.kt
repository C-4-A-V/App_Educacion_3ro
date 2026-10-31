package com.example.mpazpro.ui.screens

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.mpazpro.model.PerfilEstudiante
import com.example.mpazpro.model.ProgresoEstudiante
import com.example.mpazpro.ui.theme.MPAZPROTheme
import com.example.mpazpro.ui.components.ContenidoAdaptativo

@Preview(name = "Inicio · teléfono", widthDp = 360, heightDp = 800, showBackground = true)
@Preview(name = "Inicio · mediano", widthDp = 610, heightDp = 900, showBackground = true)
@Preview(name = "Inicio · expandido", widthDp = 900, heightDp = 1000, showBackground = true)
@Preview(name = "Inicio · horizontal", widthDp = 900, heightDp = 420, showBackground = true)
@Preview(name = "Inicio · oscuro", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Inicio · texto grande", widthDp = 320, heightDp = 800, fontScale = 1.5f)
@Composable
fun InicioAventuraPreview() {
    MPAZPROTheme {
        ContenidoAdaptativo {
        InicioEstudianteScreen(
            estudiante = PerfilEstudiante(1, "Sofía", "3.º A", null),
            progreso = ProgresoEstudiante(total = 8, completadas = 3, correctas = 2),
            onSalir = {},
            onAbrirAsignatura = {}
        )
        }
    }
}
