package com.example.mpazpro

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.example.mpazpro.ui.utils.columnasPara
import com.example.mpazpro.ui.utils.anchoParaTamano
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class WindowSizeTest {
    @Test fun respetaLosPuntosDeCorteMaterial() {
        assertEquals(WindowWidthSizeClass.Compact, anchoParaTamano(DpSize(599.dp, 800.dp)))
        assertEquals(WindowWidthSizeClass.Medium, anchoParaTamano(DpSize(600.dp, 800.dp)))
        assertEquals(WindowWidthSizeClass.Medium, anchoParaTamano(DpSize(839.dp, 800.dp)))
        assertEquals(WindowWidthSizeClass.Expanded, anchoParaTamano(DpSize(840.dp, 800.dp)))
    }

    @Test fun clasesMaterialDistribuyenUnaDosYTresColumnas() {
        assertEquals(1, columnasPara(WindowWidthSizeClass.Compact))
        assertEquals(2, columnasPara(WindowWidthSizeClass.Medium))
        assertEquals(3, columnasPara(WindowWidthSizeClass.Expanded))
    }
}
