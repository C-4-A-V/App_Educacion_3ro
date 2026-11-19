package com.example.mpazpro

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.repository.PreferenciasDocente
import com.example.mpazpro.data.repository.PaletaEstudiante
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

class PreferenciasDocenteTest {
    @Test fun conservaLaAparienciaPorCuentaAlReabrir() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val archivo = File(context.cacheDir, "docente_${UUID.randomUUID()}.preferences_pb")
        val trabajo1 = SupervisorJob()
        val trabajo2 = SupervisorJob()
        try {
            val primero = PreferenciasDocente(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + trabajo1), produceFile = { archivo }
            ))
            assertNull(primero.modoOscuro(1).first())
            assertNull(primero.paleta(1).first())
            primero.guardarPaleta(1, PaletaEstudiante.OCEANO)
            primero.guardarPaleta(2, PaletaEstudiante.ATARDECER)
            primero.guardarModoOscuro(1, true)
            primero.guardarModoOscuro(2, false)
            assertTrue(primero.modoOscuro(1).first()!!)
            assertFalse(primero.modoOscuro(2).first()!!)
            assertNull(primero.modoOscuro(3).first())
            trabajo1.cancelAndJoin()
            val segundo = PreferenciasDocente(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + trabajo2), produceFile = { archivo }
            ))
            assertEquals(PaletaEstudiante.OCEANO, segundo.paleta(1).first())
            assertEquals(PaletaEstudiante.ATARDECER, segundo.paleta(2).first())
            assertNull(segundo.paleta(3).first())
            segundo.restaurarPaleta(1)
            assertNull(segundo.paleta(1).first())
            assertEquals(PaletaEstudiante.ATARDECER, segundo.paleta(2).first())
            assertTrue(segundo.modoOscuro(1).first()!!)
            assertFalse(segundo.modoOscuro(2).first()!!)
            segundo.guardarModoOscuro(1, false)
            assertFalse(segundo.modoOscuro(1).first()!!)
            assertFalse(segundo.modoOscuro(2).first()!!)
        } finally {
            trabajo1.cancelAndJoin()
            trabajo2.cancelAndJoin()
            archivo.delete()
        }
    }
}
