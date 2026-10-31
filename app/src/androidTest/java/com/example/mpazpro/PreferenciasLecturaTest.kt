package com.example.mpazpro

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.repository.PreferenciasLectura
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

class PreferenciasLecturaTest {
    @Test fun conservaElModoAlReabrirElAlmacenamiento() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val archivo = File(context.cacheDir, "lectura_${UUID.randomUUID()}.preferences_pb")
        val trabajo1 = SupervisorJob()
        val trabajo2 = SupervisorJob()
        try {
            val primero = PreferenciasLectura(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + trabajo1), produceFile = { archivo }
            ))
            assertFalse(primero.modoLectura.first())
            primero.guardar(true)
            trabajo1.cancelAndJoin()
            val segundo = PreferenciasLectura(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + trabajo2), produceFile = { archivo }
            ))
            assertTrue(segundo.modoLectura.first())
            segundo.guardar(false)
            assertFalse(segundo.modoLectura.first())
        } finally {
            trabajo1.cancelAndJoin()
            trabajo2.cancelAndJoin()
            archivo.delete()
        }
    }
}
