package com.example.mpazpro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Ilustración vectorial local: no requiere imágenes ni conexión. */
@Composable
fun PuduExplorador(modifier: Modifier = Modifier) {
    Canvas(modifier.size(112.dp).semantics { contentDescription = "Pudú explorador" }) {
        val escala = size.minDimension / 112f
        fun punto(x: Float, y: Float) = Offset(x * escala, y * escala)
        val pelaje = Color(0xFFB87546)
        val claro = Color(0xFFFFD8A8)
        drawCircle(Color(0xFFDCEFD8), 54 * escala, punto(56f, 56f))
        drawOval(pelaje, punto(18f, 16f), Size(27 * escala, 47 * escala))
        drawOval(pelaje, punto(68f, 16f), Size(27 * escala, 47 * escala))
        drawOval(claro, punto(25f, 23f), Size(13 * escala, 29 * escala))
        drawOval(claro, punto(75f, 23f), Size(13 * escala, 29 * escala))
        drawOval(pelaje, punto(23f, 37f), Size(66 * escala, 65 * escala))
        drawOval(claro, punto(35f, 64f), Size(42 * escala, 32 * escala))
        drawCircle(Color(0xFF34271F), 3.5f * escala, punto(41f, 61f))
        drawCircle(Color(0xFF34271F), 3.5f * escala, punto(71f, 61f))
        drawCircle(Color.White, 1.2f * escala, punto(42f, 60f))
        drawCircle(Color.White, 1.2f * escala, punto(72f, 60f))
        drawOval(Color(0xFF34271F), punto(50f, 72f), Size(12 * escala, 8 * escala))
        drawLine(Color(0xFF34271F), punto(56f, 79f), punto(56f, 84f), 2 * escala)
        drawLine(Color(0xFF34271F), punto(56f, 84f), punto(61f, 87f), 2 * escala)
        val panuelo = Path().apply {
            moveTo(30 * escala, 95 * escala)
            lineTo(82 * escala, 95 * escala)
            lineTo(59 * escala, 110 * escala)
            close()
        }
        drawPath(panuelo, Color(0xFF34796B))
    }
}
