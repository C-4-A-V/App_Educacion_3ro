package com.example.mpazpro

import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.remote.ClienteDocenteAutenticado
import com.example.mpazpro.data.repository.DocenteRemotoRepository
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Prueba optativa contra Spring aislado; requiere el argumento docenteUrl. */
class DocenteRemotoSpringTest {
    @Test fun registroAccesoCursoYCierre() = runBlocking {
        val url = InstrumentationRegistry.getArguments().getString("docenteUrl")
        assumeTrue("Requiere un servidor aislado", !url.isNullOrBlank())
        val repo = DocenteRemotoRepository(ClienteDocenteAutenticado(requireNotNull(url)))
        val correo = "prueba-${UUID.randomUUID()}@ejemplo.cl"
        val clave = "Clave-prueba-2026"
        assertEquals(correo, repo.registrar(correo, clave).correo)
        assertNull(repo.sesion.value)
        assertEquals(correo, repo.entrar(correo, clave).correo)
        assertEquals(correo, repo.recuperarSesion().correo)
        assertTrue(repo.cursos().isEmpty())
        val nuevo = repo.crearCurso("Z", java.util.UUID.randomUUID().toString())
        assertTrue(nuevo.id > 0)
        assertTrue(repo.cursos().any { it.id == nuevo.id })
        repo.salir()
        assertNull(repo.sesion.value)
    }
}
