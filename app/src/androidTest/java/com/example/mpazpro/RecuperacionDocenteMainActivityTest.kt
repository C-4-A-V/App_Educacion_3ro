package com.example.mpazpro

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.CursoEntity
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.DocenteRepository
import com.example.mpazpro.data.repository.ResultadoRegistroDocente
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class RecuperacionDocenteMainActivityTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun recuperaPerfilAnteriorDesdeMainActivity() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("pruebaServidorDocente") == "si")
        val sufijo = UUID.randomUUID().toString()
        val anterior = "anterior-$sufijo@ejemplo.cl"
        val correo = "nuevo-$sufijo@ejemplo.cl"
        val claveLocal = "Clave-local-2026"
        val claveRemota = "Clave-demo-2026"
        val base = MpazDatabase.obtenerInstancia(InstrumentationRegistry.getInstrumentation().targetContext)
        runBlocking {
            val registro = DocenteRepository(base.docenteDao()).registrar("Docente anterior", anterior, claveLocal)
                as ResultadoRegistroDocente.Registrado
            base.cursoDao().insertar(CursoEntity(docenteId = registro.docenteId, nombre = "3.º Básico Z"))
        }

        compose.waitUntil(20_000) { compose.onAllNodesWithText("Ingresar como docente").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Ingresar como docente").performScrollTo().performClick()
        compose.onNodeWithText("No tengo cuenta: registrarme").performScrollTo().performClick()
        compose.onNodeWithText("Nombre", substring = false).performScrollTo().performTextInput("Docente ficticio")
        compose.onNodeWithText("Correo electrónico").performScrollTo().performTextInput(correo)
        compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput(claveRemota)
        compose.onNodeWithText("Confirmar contraseña").performScrollTo().performTextInput(claveRemota)
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Crear cuenta", substring = false).performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Acceso docente").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Correo electrónico").performScrollTo().performTextInput(correo)
        compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput(claveRemota)
        Espresso.closeSoftKeyboard()
        compose.onNode(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, Role.Checkbox)).performScrollTo().performClick()
        compose.onNodeWithText("Ingresar", substring = false).performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Recuperar mis datos guardados").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Correo anterior").performTextInput(anterior)
        compose.onNodeWithText("Contraseña anterior").performTextInput(claveLocal)
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Recuperar", substring = false).performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Cuenta: $correo").fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("1 cursos a tu cargo", substring = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Hola, Docente").assertExists()
        compose.onNodeWithTag("panelDocenteLista").performScrollToNode(hasText("Cerrar sesión"))
        compose.onNodeWithText("Cerrar sesión").performClick()
    }
}
