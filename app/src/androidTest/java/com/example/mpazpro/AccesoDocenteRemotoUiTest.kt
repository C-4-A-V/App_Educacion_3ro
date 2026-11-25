package com.example.mpazpro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import java.util.UUID
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class AccesoDocenteRemotoUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun registraIngresaYSaleEnServidorAislado() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("pruebaServidorDocente") == "si")
        val correo = "ui-${UUID.randomUUID()}@ejemplo.cl"
        val clave = "Clave-demo-2026"
        compose.waitUntil(20_000) { compose.onAllNodesWithText("Ingresar como docente").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Ingresar como docente").performScrollTo().performClick()
        compose.onNodeWithText("No tengo cuenta: registrarme").performScrollTo().performClick()
        compose.onNodeWithText("Nombre", substring = false).performScrollTo().performTextInput("Docente prueba")
        compose.onNodeWithText("Correo electrónico").performScrollTo().performTextInput(correo)
        compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput(clave)
        compose.onNodeWithText("Confirmar contraseña").performScrollTo().performTextInput(clave)
        compose.onNodeWithText("Crear cuenta", substring = false).performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Acceso docente").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Correo electrónico").performScrollTo().performTextInput(correo)
        compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput("Clave-incorrecta")
        compose.onNodeWithText("Ingresar", substring = false).performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Correo o contraseña incorrectos.").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput(clave)
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Ingresar", substring = false).performScrollTo().performClick()
        try {
            compose.waitUntil(30_000) { compose.onAllNodesWithText("Cuenta: $correo", substring = false).fetchSemanticsNodes().isNotEmpty() }
        } catch (error: Throwable) {
            throw AssertionError(compose.onRoot().printToString(), error)
        }
        compose.onNodeWithText("Cuenta: $correo").assertExists()
        val base = com.example.mpazpro.data.local.MpazDatabase.obtenerInstancia(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
        val idLocal = kotlinx.coroutines.runBlocking {
            val local = requireNotNull(base.docenteDao().buscarPorCorreo(correo))
            base.cursoDao().insertar(com.example.mpazpro.data.local.CursoEntity(
                docenteId = local.id, nombre = "3.º Básico Z"
            ))
            local.id
        }
        compose.onNodeWithTag("panelDocenteLista").performScrollToNode(hasText("Cerrar sesión"))
        compose.onNodeWithText("Cerrar sesión").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Ingresar como docente").fetchSemanticsNodes().isNotEmpty() }
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Ingresar como docente").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Ingresar como docente").performScrollTo().performClick()
        compose.onNodeWithText("Correo electrónico").performScrollTo().performTextInput(correo)
        compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput(clave)
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Ingresar", substring = false).performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Cuenta: $correo").fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("1 cursos a tu cargo", substring = true).fetchSemanticsNodes().isNotEmpty() }
        kotlinx.coroutines.runBlocking { assert(idLocal == base.docenteDao().buscarPorCorreo(correo)?.id) }
        compose.onNodeWithTag("panelDocenteLista").performScrollToNode(hasText("Cerrar sesión"))
        compose.onNodeWithText("Cerrar sesión").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Ingresar como docente").fetchSemanticsNodes().isNotEmpty() }

        val otra = "ui-otra-${UUID.randomUUID()}@ejemplo.cl"
        compose.onNodeWithText("Ingresar como docente").performScrollTo().performClick()
        compose.onNodeWithText("No tengo cuenta: registrarme").performScrollTo().performClick()
        compose.onNodeWithText("Nombre", substring = false).performScrollTo().performTextInput("Otra docente")
        compose.onNodeWithText("Correo electrónico").performScrollTo().performTextInput(otra)
        compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput(clave)
        compose.onNodeWithText("Confirmar contraseña").performScrollTo().performTextInput(clave)
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Crear cuenta", substring = false).performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Acceso docente").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Correo electrónico").performScrollTo().performTextInput(otra)
        compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput(clave)
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Ingresar", substring = false).performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Cuenta: $otra").fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("0 cursos a tu cargo", substring = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Cuenta: $correo").assertDoesNotExist()
        compose.onNodeWithTag("panelDocenteLista").performScrollToNode(hasText("Cerrar sesión"))
        compose.onNodeWithText("Cerrar sesión").performClick()
    }
}
