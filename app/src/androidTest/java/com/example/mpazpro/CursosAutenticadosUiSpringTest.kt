package com.example.mpazpro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.remote.ClienteDocenteAutenticado
import com.example.mpazpro.data.repository.DocenteRemotoRepository
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Requiere un catálogo aislado sin cursos, accesible en BuildConfig.URL_SERVIDOR. */
class CursosAutenticadosUiSpringTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun confirmaCreacionYNoDuplicaAlActualizar() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("pruebaServidorDocente") == "si")
        val correo = "curso-ui-${UUID.randomUUID()}@ejemplo.cl"
        val clave = "Clave-prueba-2026"
        val observador = DocenteRemotoRepository(ClienteDocenteAutenticado(BuildConfig.URL_SERVIDOR))
        runBlocking {
            observador.registrar(correo, clave)
            observador.entrar(correo, clave)
            assertEquals(0, observador.cursos().size)
        }
        compose.waitUntil(20_000) { compose.onAllNodesWithText("Ingresar como docente").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Ingresar como docente").performScrollTo().performClick()
        compose.onNodeWithText("Correo electrónico").performScrollTo().performTextInput(correo)
        compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput(clave)
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Ingresar", substring = false).performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Cuenta: $correo").fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(20_000) { compose.onAllNodesWithText("0 cursos a tu cargo", substring = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Mis cursos").performScrollTo().performClick()
        compose.onNodeWithText("Crear nuevo curso").performClick()
        compose.onNodeWithText("¿Crear nuevo curso?").assertExists()
        runBlocking { assertEquals(0, observador.cursos().size) }
        compose.onNodeWithText("Crear curso", substring = false).performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("3.º Básico A").fetchSemanticsNodes().isNotEmpty() }
        val original = runBlocking { observador.cursos().single() }
        compose.onNodeWithText("Cerrar", substring = false).performClick()
        compose.onNodeWithContentDescription("Actualizar cursos").performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Estado de actualización").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Entendido").performClick()
        assertEquals(original.id, runBlocking { observador.cursos().single().id })
        compose.onNodeWithTag("panelDocenteLista").performScrollToNode(hasText("Cerrar sesión"))
        compose.onNodeWithText("Cerrar sesión").performClick()
    }
}
