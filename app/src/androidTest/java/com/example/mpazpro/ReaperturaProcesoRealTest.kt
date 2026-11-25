package com.example.mpazpro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.CursoEntity
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.remote.ClienteDocenteAutenticado
import com.example.mpazpro.data.repository.DocenteRemotoRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/**
 * Ejecutar cada método por separado, con el mismo sufijo. Entre ambos, terminar el
 * proceso mediante adb force-stop y volver a iniciar MainActivity, sin limpiar datos.
 * Requiere un servidor aislado con catálogo persistente y cuentas ficticias.
 */
class ReaperturaProcesoRealTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val args get() = InstrumentationRegistry.getArguments()
    private val clave = "Clave-prueba-2026"
    private val sufijo get() = requireNotNull(args.getString("sufijoProceso"))
    private val correo get() = "proceso-$sufijo@ejemplo.cl"
    private val otro get() = "proceso-otro-$sufijo@ejemplo.cl"
    private val base get() = MpazDatabase.obtenerInstancia(InstrumentationRegistry.getInstrumentation().targetContext)

    private fun exigirServidorAislado() {
        assumeTrue(args.getString("pruebaServidorDocente") == "si")
    }

    private fun ingresar(cuenta: String) {
        compose.waitUntil(20_000) { compose.onAllNodesWithText("Ingresar como docente").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Ingresar como docente").performScrollTo().performClick()
        compose.onNodeWithText("Correo electrónico").performScrollTo().performTextInput(cuenta)
        compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput(clave)
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Ingresar", substring = false).performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Cuenta: $cuenta").fetchSemanticsNodes().isNotEmpty() }
    }

    private fun salir() {
        compose.onNodeWithTag("panelDocenteLista").performScrollToNode(hasText("Cerrar sesión"))
        compose.onNodeWithText("Cerrar sesión").performClick()
    }

    @Test fun prepara() {
        exigirServidorAislado()
        runBlocking {
            val remoto = DocenteRemotoRepository(ClienteDocenteAutenticado(BuildConfig.URL_SERVIDOR))
            remoto.registrar(correo, clave)
            remoto.registrar(otro, clave)
        }
        ingresar(correo)
        runBlocking {
            val local = requireNotNull(base.docenteDao().buscarPorCorreo(correo))
            val id = base.cursoDao().insertar(CursoEntity(docenteId = local.id, nombre = "3.º Básico Z"))
            check(id > 0)
            android.util.Log.i("ReaperturaProcesoReal", "PERFIL_LOCAL_ID=${local.id}")
        }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("1 cursos a tu cargo", substring = true).fetchSemanticsNodes().isNotEmpty() }
        // No cerrar sesión: el llamador terminará el proceso completo.
    }

    @Test fun reabre() {
        exigirServidorAislado()
        val idEsperado = requireNotNull(args.getString("perfilEsperado")).toLong()
        // Tras matar el proceso el arranque no debe restaurar una sesión autenticada.
        compose.waitUntil(20_000) { compose.onAllNodesWithText("Ingresar como docente").fetchSemanticsNodes().isNotEmpty() }
        ingresar(correo)
        compose.waitUntil(10_000) { compose.onAllNodesWithText("1 cursos a tu cargo", substring = true).fetchSemanticsNodes().isNotEmpty() }
        runBlocking {
            val local = requireNotNull(base.docenteDao().buscarPorCorreo(correo))
            assertEquals(idEsperado, local.id)
            assertEquals(1, base.cursoDao().listarPorDocente(local.id).size)
        }
        salir()
        ingresar(otro)
        compose.waitUntil(10_000) { compose.onAllNodesWithText("0 cursos a tu cargo", substring = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Cuenta: $correo").assertDoesNotExist()
        salir()
    }
}
