package com.example.mpazpro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.mpazpro.ui.utils.LocalAnchoVentana
import com.example.mpazpro.ui.utils.anchoParaTamano

/** Usa el espacio disponible: funciona en ventanas, pantalla dividida y previews. */
@Composable
fun ContenidoAdaptativo(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val ancho = anchoParaTamano(DpSize(maxWidth, maxHeight))
        CompositionLocalProvider(LocalAnchoVentana provides ancho) {
            Box(Modifier.widthIn(max = 1200.dp).fillMaxWidth()) { content() }
        }
    }
}
