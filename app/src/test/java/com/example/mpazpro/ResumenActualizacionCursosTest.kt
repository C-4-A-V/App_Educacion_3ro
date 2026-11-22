package com.example.mpazpro

import com.example.mpazpro.model.ConexionCurso
import com.example.mpazpro.model.CursosUiState
import com.example.mpazpro.model.resumenActualizacionCursos
import org.junit.Assert.*
import org.junit.Test

class ResumenActualizacionCursosTest {
    @Test fun informaLimitesInclusoCuandoLosCursosSeConectan() {
        val mensaje = resumenActualizacionCursos(CursosUiState(conexiones = mapOf(1L to ConexionCurso.CONECTADO)), false)
        assertTrue(mensaje.startsWith("Cursos actualizados."))
        assertTrue(mensaje.contains("estudiantes, fotos y resultados"))
        assertTrue(mensaje.contains("todavía no se sincronizan"))
    }

    @Test fun noInformaExitoSiFallaLaConexionInclusoSinCursosLocales() {
        val mensaje = resumenActualizacionCursos(CursosUiState(), true)
        assertTrue(mensaje.startsWith("No pudimos conectar"))
    }

    @Test fun advierteSiAlgunCursoRequiereRevision() {
        val mensaje = resumenActualizacionCursos(CursosUiState(conexiones = mapOf(1L to ConexionCurso.REVISAR)), false)
        assertTrue(mensaje.startsWith("Algunos cursos necesitan revisión"))
    }
}
