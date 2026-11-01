package com.example.mpazpro

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.repository.PreferenciasEstudiante
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

class PreferenciasEstudianteTest {
    @Test fun conservaObjetivoYAparienciaSeparadosPorPerfil() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val archivo = File(context.cacheDir, "estudiante_${UUID.randomUUID()}.preferences_pb")
        val trabajo1 = SupervisorJob()
        val trabajo2 = SupervisorJob()
        try {
            val primero = PreferenciasEstudiante(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + trabajo1), produceFile = { archivo }
            ))
            assertNull(primero.observar(1).first().modoOscuro)
            primero.guardarOscuro(1, true)
            primero.guardarTextoGrande(1, true)
            primero.guardarObjetivo(1, "Leer un cuento con mi familia")
            primero.guardarOscuro(2, false)
            assertFalse(primero.observar(2).first().textoGrande)
            assertEquals("", primero.observar(2).first().objetivo)
            trabajo1.cancelAndJoin()
            val segundo = PreferenciasEstudiante(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + trabajo2), produceFile = { archivo }
            ))
            val perfil1 = segundo.observar(1).first()
            assertEquals(true, perfil1.modoOscuro)
            assertTrue(perfil1.textoGrande)
            assertEquals("Leer un cuento con mi familia", perfil1.objetivo)
            assertEquals(false, segundo.observar(2).first().modoOscuro)
            segundo.guardarOscuro(1, false)
            segundo.guardarTextoGrande(1, false)
            assertEquals(false, segundo.observar(1).first().modoOscuro)
            assertFalse(segundo.observar(1).first().textoGrande)
            assertEquals("Leer un cuento con mi familia", segundo.observar(1).first().objetivo)
        } finally {
            trabajo1.cancelAndJoin()
            trabajo2.cancelAndJoin()
            archivo.delete()
        }
    }
}
