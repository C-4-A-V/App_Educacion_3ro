package com.example.mpazpro.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.ui.viewmodel.PreferenciasViewModel

@Composable
fun LecturaComoda(content: @Composable () -> Unit) {
    val preferencias: PreferenciasViewModel = viewModel()
    val lectura by preferencias.lectura.collectAsStateWithLifecycle()
    val sistema = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(sistema.density, sistema.fontScale * if (lectura) 1.15f else 1f)
    ) { content() }
}
