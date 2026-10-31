package com.example.mpazpro

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.media.FotoStorage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class FotoStorageTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun importaImagenPrivadaYEliminaLaCapturaTemporal() = runBlocking {
        val fotos = FotoStorage(context)
        val uri = fotos.crearTemporalCamara()
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.GREEN) }
        context.contentResolver.openOutputStream(uri)!!.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val destino = File(fotos.importar(uri))
        try {
            assertTrue(destino.isFile)
            assertTrue(destino.length() > 0)
            assertThrows(java.io.FileNotFoundException::class.java) { context.contentResolver.openInputStream(uri)?.close() }
        } finally {
            destino.delete()
        }
        Unit
    }

    @Test fun rechazaFormatoInvalidoYLimpiaTemporalCancelado() = runBlocking {
        val fotos = FotoStorage(context)
        val uri = fotos.crearTemporalCamara()
        context.contentResolver.openOutputStream(uri)!!.use { it.write(byteArrayOf(1, 2, 3)) }
        try {
            fotos.importar(uri)
            fail("No debe aceptar un archivo que no es una imagen")
        } catch (_: IllegalArgumentException) {
            // El formato inválido se rechaza y el temporal se elimina en finally.
        }
        assertThrows(java.io.FileNotFoundException::class.java) { context.contentResolver.openInputStream(uri)?.close() }
        val cancelada = fotos.crearTemporalCamara()
        fotos.descartarTemporal(cancelada)
        assertThrows(java.io.FileNotFoundException::class.java) { context.contentResolver.openInputStream(cancelada)?.close() }
        Unit
    }
}
