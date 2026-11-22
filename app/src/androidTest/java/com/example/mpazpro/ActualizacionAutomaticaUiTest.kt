package com.example.mpazpro

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mpazpro.ui.screens.ActualizacionAutomatica
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ActualizacionAutomaticaUiTest {
    @get:Rule val compose = createAndroidComposeRule<PruebaComposeActivity>()

    @Test fun actualizaAlReanudarYSeDetieneCuandoLaPantallaNoEstaVisible() {
        val propietario = object : LifecycleOwner {
            val registro = LifecycleRegistry(this)
            override val lifecycle: Lifecycle get() = registro
        }
        val consultas = AtomicInteger()
        compose.runOnUiThread { propietario.registro.currentState = Lifecycle.State.CREATED }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides propietario) {
                ActualizacionAutomatica("curso", 100L) { consultas.incrementAndGet() }
            }
        }
        compose.runOnIdle { assertEquals(0, consultas.get()) }
        compose.runOnUiThread { propietario.registro.currentState = Lifecycle.State.RESUMED }
        compose.waitUntil(5_000) { consultas.get() >= 2 }
        compose.runOnUiThread { propietario.registro.currentState = Lifecycle.State.STARTED }
        compose.waitForIdle()
        val alPausar = consultas.get()
        Thread.sleep(350)
        assertEquals(alPausar, consultas.get())
        compose.runOnUiThread { propietario.registro.currentState = Lifecycle.State.RESUMED }
        compose.waitUntil(5_000) { consultas.get() > alPausar }
        compose.runOnUiThread { propietario.registro.currentState = Lifecycle.State.DESTROYED }
    }
}
