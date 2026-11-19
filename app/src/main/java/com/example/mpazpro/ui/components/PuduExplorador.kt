package com.example.mpazpro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

enum class AnimoPudu { Tranquilo, Pensando, Alegre }

/** Mascota de formas suaves y colores planos, legible también a 48 dp. */
@Composable
fun PuduExplorador(modifier: Modifier = Modifier, animo: AnimoPudu = AnimoPudu.Tranquilo) {
    val trazos = remember { TrazosPudu() }
    val pelaje = Color(0xFFC98D59)
    val borde = Color(0xFF855734)
    val tinta = Color(0xFF473329)
    Canvas(modifier.size(112.dp).semantics {
        contentDescription = when (animo) {
            AnimoPudu.Alegre -> "Pudú explorador celebrando tu esfuerzo"
            AnimoPudu.Pensando -> "Pudú explorador pensando contigo"
            else -> "Pudú explorador"
        }
    }) {
        val lado = size.minDimension
        translate((size.width - lado) / 2f, (size.height - lado) / 2f) {
            scale(lado / 112f, lado / 112f, pivot = Offset.Zero) {
                drawCircle(Color(0xFFE2F1D9), 53f, Offset(56f, 56f))
                drawOval(Color(0xFFC4D9BC), Offset(28f, 101f), Size(56f, 6f))
                drawOval(Color(0xFFB57A49), Offset(29f, 83f), Size(54f, 25f))

                drawPath(trazos.orejaIzquierda, pelaje)
                drawPath(trazos.orejaDerecha, pelaje)
                drawPath(trazos.orejaIzquierda, borde, style = Stroke(1.6f))
                drawPath(trazos.orejaDerecha, borde, style = Stroke(1.6f))
                drawPath(trazos.interiorIzquierdo, Color(0xFFF1BE99))
                drawPath(trazos.interiorDerecho, Color(0xFFF1BE99))

                drawPath(trazos.cabeza, pelaje)
                drawPath(trazos.cabeza, borde, style = Stroke(1.6f))
                // Un pequeño mechón, sin textura realista de pelaje.
                drawLine(Color(0xFFE6B681), Offset(50f, 38f), Offset(48f, 43f), 2f, StrokeCap.Round)
                drawLine(Color(0xFFE6B681), Offset(56f, 36f), Offset(55f, 42f), 2f, StrokeCap.Round)
                drawLine(Color(0xFFE6B681), Offset(62f, 38f), Offset(62f, 43f), 2f, StrokeCap.Round)

                // Mejillas rosadas y ojos sencillos de dibujo animado.
                drawOval(Color(0xFFDF9B7C), Offset(28f, 71f), Size(13f, 7f))
                drawOval(Color(0xFFDF9B7C), Offset(71f, 71f), Size(13f, 7f))
                if (animo == AnimoPudu.Alegre) {
                    drawArc(tinta, 190f, 160f, false, Offset(33f, 59f), Size(13f, 13f),
                        style = Stroke(2.8f, cap = StrokeCap.Round))
                    drawArc(tinta, 190f, 160f, false, Offset(66f, 59f), Size(13f, 13f),
                        style = Stroke(2.8f, cap = StrokeCap.Round))
                } else {
                    drawOval(tinta, Offset(35f, 58f), Size(10f, 14f))
                    drawOval(tinta, Offset(67f, 58f), Size(10f, 14f))
                    drawCircle(Color(0xFFFFFCF1), 2.3f, Offset(38f, 61f))
                    drawCircle(Color(0xFFFFFCF1), 2.3f, Offset(70f, 61f))
                    if (animo == AnimoPudu.Pensando) {
                        drawLine(tinta, Offset(34f, 53f), Offset(44f, 50f), 2f, StrokeCap.Round)
                        drawLine(tinta, Offset(68f, 52f), Offset(76f, 54f), 1.8f, StrokeCap.Round)
                    }
                }

                drawOval(Color(0xFFF3CCA0), Offset(40f, 74f), Size(32f, 21f))
                drawPath(trazos.nariz, tinta)
                drawLine(tinta, Offset(56f, 82f), Offset(56f, 86f), 1.5f, StrokeCap.Round)
                if (animo == AnimoPudu.Pensando) {
                    drawArc(tinta, 25f, 95f, false, Offset(51f, 83f), Size(11f, 8f),
                        style = Stroke(1.7f, cap = StrokeCap.Round))
                } else {
                    drawArc(tinta, 15f, 150f, false, Offset(48f, 81f),
                        Size(16f, if (animo == AnimoPudu.Alegre) 13f else 10f),
                        style = Stroke(1.8f, cap = StrokeCap.Round))
                }

                drawPath(trazos.panuelo, Color(0xFF348873))
                drawPath(trazos.pliegue, Color(0xFF216650))
                drawLine(Color(0xFF90C8AB), Offset(35f, 96f), Offset(54f, 101f), 1.6f, StrokeCap.Round)
                drawCircle(Color(0xFFFFDC83), 2.7f, Offset(57f, 100f))
            }
        }
    }
}

/** Coordenadas normalizadas; los trazos se reutilizan entre repintados. */
private class TrazosPudu {
    val orejaIzquierda = Path().apply {
        moveTo(39f, 49f); cubicTo(25f, 49f, 14f, 37f, 15f, 27f)
        cubicTo(16f, 20f, 27f, 24f, 33f, 30f)
        quadraticTo(43f, 39f, 39f, 49f); close()
    }
    val orejaDerecha = Path().apply {
        moveTo(73f, 49f); cubicTo(87f, 49f, 98f, 37f, 97f, 27f)
        cubicTo(96f, 20f, 85f, 24f, 79f, 30f)
        quadraticTo(69f, 39f, 73f, 49f); close()
    }
    val interiorIzquierdo = Path().apply {
        moveTo(34f, 43f); quadraticTo(23f, 38f, 22f, 29f)
        quadraticTo(34f, 32f, 34f, 43f); close()
    }
    val interiorDerecho = Path().apply {
        moveTo(78f, 43f); quadraticTo(89f, 38f, 90f, 29f)
        quadraticTo(78f, 32f, 78f, 43f); close()
    }
    val cabeza = Path().apply {
        moveTo(56f, 31f); cubicTo(35f, 30f, 24f, 45f, 25f, 65f)
        cubicTo(25f, 84f, 37f, 98f, 56f, 98f)
        cubicTo(75f, 98f, 87f, 84f, 87f, 65f)
        cubicTo(88f, 45f, 77f, 30f, 56f, 31f); close()
    }
    val nariz = Path().apply {
        moveTo(50f, 77f); quadraticTo(56f, 74f, 62f, 77f)
        quadraticTo(63f, 80f, 58f, 83f); quadraticTo(56f, 85f, 54f, 83f)
        quadraticTo(49f, 80f, 50f, 77f); close()
    }
    val panuelo = Path().apply {
        moveTo(33f, 93f); quadraticTo(56f, 102f, 79f, 93f)
        lineTo(72f, 101f); lineTo(57f, 110f); lineTo(41f, 102f); close()
    }
    val pliegue = Path().apply {
        moveTo(58f, 101f); lineTo(72f, 101f); lineTo(57f, 110f); close()
    }
}
