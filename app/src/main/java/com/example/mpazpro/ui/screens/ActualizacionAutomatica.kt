package com.example.mpazpro.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Consulta al entrar o volver a la pantalla y mientras permanece en primer plano. */
@Composable
internal fun ActualizacionAutomatica(
    clave: Any,
    intervaloMs: Long = 20_000L,
    actualizar: () -> Unit
) {
    val owner = LocalLifecycleOwner.current
    val accion by rememberUpdatedState(actualizar)
    LaunchedEffect(owner, clave, intervaloMs) {
        require(intervaloMs > 0)
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                accion()
                delay(intervaloMs)
            }
        }
    }
}
