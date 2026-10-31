package com.example.mpazpro.ui.utils

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize

/** La raíz publica el tamaño disponible, también al renderizar previews. */
val LocalAnchoVentana = compositionLocalOf<WindowWidthSizeClass?> { null }

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun anchoParaTamano(tamano: DpSize): WindowWidthSizeClass =
    WindowSizeClass.calculateFromSize(tamano).widthSizeClass

@Composable
fun claseAnchoVentana(): WindowWidthSizeClass {
    LocalAnchoVentana.current?.let { return it }
    val ventana = LocalWindowInfo.current.containerSize
    val tamano = with(LocalDensity.current) { DpSize(ventana.width.toDp(), ventana.height.toDp()) }
    return anchoParaTamano(tamano)
}

fun columnasPara(ancho: WindowWidthSizeClass): Int = when (ancho) {
    WindowWidthSizeClass.Expanded -> 3
    WindowWidthSizeClass.Medium -> 2
    else -> 1
}
