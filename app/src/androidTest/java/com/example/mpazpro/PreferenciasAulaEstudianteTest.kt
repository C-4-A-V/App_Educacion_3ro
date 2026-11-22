package com.example.mpazpro

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.repository.IntentoAulaPendiente
import com.example.mpazpro.data.repository.PreferenciasAulaEstudiante
import java.io.File
import java.util.UUID
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class PreferenciasAulaEstudianteTest {
    @Test fun conservaTokenYRespuestaPendienteSinMezclarEstudiantes() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val archivo = File(contexto.cacheDir, "aula_test_${UUID.randomUUID()}.preferences_pb")
        val trabajo1 = SupervisorJob()
        val trabajo2 = SupervisorJob()
        val catalogo = "11111111-1111-1111-1111-111111111111"
        try {
            val primero = PreferenciasAulaEstudiante(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + trabajo1), produceFile = { archivo }
            ))
            val token1 = primero.token(1, catalogo)
            assertEquals(token1, primero.token(1, catalogo))
            assertNotEquals(token1, primero.token(2, catalogo))
            val intento = IntentoAulaPendiente(1, 2)
            primero.preparar(1, catalogo, 7, intento)
            primero.preparar(1, catalogo, 7, intento)
            try {
                primero.preparar(1, catalogo, 7, IntentoAulaPendiente(1, 3))
                fail("No debe reemplazar una respuesta pendiente")
            } catch (_: IllegalStateException) {}
            trabajo1.cancelAndJoin()
            val segundo = PreferenciasAulaEstudiante(PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + trabajo2), produceFile = { archivo }
            ))
            assertEquals(token1, segundo.token(1, catalogo))
            assertEquals(intento, segundo.pendiente(1, catalogo, 7))
            assertNull(segundo.pendiente(2, catalogo, 7))
            segundo.confirmar(1, catalogo, 7, intento)
            assertNull(segundo.pendiente(1, catalogo, 7))
        } finally {
            trabajo1.cancelAndJoin()
            trabajo2.cancelAndJoin()
            archivo.delete()
        }
    }
}
