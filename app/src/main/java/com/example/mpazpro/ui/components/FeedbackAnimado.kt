package com.example.mpazpro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** Se ejecuta al entrar un resultado nuevo, sin intervenir en su guardado. */
@Composable
fun FeedbackAnimado(content: @Composable () -> Unit) {
    val visibilidad = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(visibleState = visibilidad, enter = fadeIn() + expandVertically()) { content() }
}
