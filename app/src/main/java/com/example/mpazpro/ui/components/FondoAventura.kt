package com.example.mpazpro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Path

/** Decoración vectorial local: cielo, sol y colinas sin descargas. */
@Composable
fun FondoAventura(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val oscuro = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cielo = MaterialTheme.colorScheme.background
    Box(modifier.background(cielo)) {
        Canvas(Modifier.matchParentSize()) {
            val amarillo = if (oscuro) Color(0xFF7D6128) else Color(0xFFFFDB72)
            drawCircle(amarillo, 76f, Offset(size.width - 24f, 62f))
            drawCircle(Color.White.copy(alpha = if (oscuro) .05f else .7f), 110f, Offset(-25f, 260f))
            val colina = Path().apply {
                moveTo(0f, size.height * .90f)
                quadraticTo(size.width * .35f, size.height * .77f, size.width, size.height * .94f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(colina, if (oscuro) Color(0xFF203E3A) else Color(0xFFD6F4DF))
            val colinaFrente = Path().apply {
                moveTo(0f, size.height * .97f)
                quadraticTo(size.width * .7f, size.height * .84f, size.width, size.height * .90f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(colinaFrente, if (oscuro) Color(0xFF254B44) else Color(0xFFBDEBCF))
            listOf(.12f to .12f, .87f to .35f, .08f to .62f).forEach { (x, y) ->
                val centro = Offset(size.width * x, size.height * y)
                drawLine(amarillo, centro - Offset(9f, 0f), centro + Offset(9f, 0f), 4f)
                drawLine(amarillo, centro - Offset(0f, 9f), centro + Offset(0f, 9f), 4f)
            }
        }
        content()
    }
}
