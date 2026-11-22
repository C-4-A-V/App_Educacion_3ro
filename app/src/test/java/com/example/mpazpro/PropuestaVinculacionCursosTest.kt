package com.example.mpazpro

import com.example.mpazpro.data.repository.CursoParaVincular
import com.example.mpazpro.data.repository.PropuestaCurso
import com.example.mpazpro.data.repository.PropuestaVinculacionCursos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PropuestaVinculacionCursosTest {
    @Test fun reconoceFormatosYNoConfundeOtrosCursos() {
        assertEquals('A', PropuestaVinculacionCursos.seccion("3.º A"))
        assertEquals('A', PropuestaVinculacionCursos.seccion("3.º Básico A"))
        assertEquals('B', PropuestaVinculacionCursos.seccion("3 basico b"))
        assertNull(PropuestaVinculacionCursos.seccion("3.º Básico A tarde"))
        assertEquals('B', PropuestaVinculacionCursos.seccion("3°b"))
        assertEquals('C', PropuestaVinculacionCursos.seccion("3 C"))
        assertNull(PropuestaVinculacionCursos.seccion("13 A"))
        assertNull(PropuestaVinculacionCursos.seccion("3 A tarde"))
    }

    @Test fun proponeFormatoUniformeSinModificarNombres() {
        assertEquals("3.º Básico A", PropuestaVinculacionCursos.nombreVisible("3A"))
        assertEquals("3.º Básico B", PropuestaVinculacionCursos.nombreVisible("3. BASICO B"))
        assertEquals("Nombre distinto", PropuestaVinculacionCursos.nombreVisible("Nombre distinto"))
        assertEquals('A', PropuestaVinculacionCursos.siguiente(emptyList()))
        assertEquals('B', PropuestaVinculacionCursos.siguiente(listOf("3.º Básico A")))
        assertEquals('C', PropuestaVinculacionCursos.siguiente(listOf("3A", "3. BASICO B")))
        assertNull(PropuestaVinculacionCursos.siguiente(listOf("3A", "3.º Básico A")))
        assertNull(PropuestaVinculacionCursos.siguiente(listOf("Otro curso")))
    }

    @Test fun proponeSoloCoincidenciasUnicas() {
        val locales = listOf(CursoParaVincular(1, "3 A"), CursoParaVincular(2, "3.º B"), CursoParaVincular(3, "3 B"))
        val remoto = listOf(CursoParaVincular(10, "3° A"), CursoParaVincular(11, "3° B"))
        val resultado = PropuestaVinculacionCursos.comparar(locales, remoto)
        assertEquals(listOf(PropuestaCurso(10, 1)), resultado.propuestas)
        assertEquals(setOf('B'), resultado.seccionesAmbiguas)
    }
}
