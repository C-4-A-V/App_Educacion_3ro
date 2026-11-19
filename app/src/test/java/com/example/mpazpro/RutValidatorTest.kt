package com.example.mpazpro

import com.example.mpazpro.validation.RutValidator
import org.junit.Assert.*
import org.junit.Test

class RutValidatorTest {
    @Test fun aceptaYNormalizaRutValido() {
        assertEquals("123456785", RutValidator.normalizar("12.345.678-5"))
        assertEquals("12345670K", RutValidator.normalizar("12.345.670-k"))
    }

    @Test fun rechazaDigitosVerificadoresYFormatosInvalidos() {
        assertNull(RutValidator.normalizar("12.345.678-9"))
        assertNull(RutValidator.normalizar("123"))
        assertNull(RutValidator.normalizar("12.345.678-x"))
    }

    @Test fun exigeCuatroDigitosAsciiEnPin() {
        assertTrue(RutValidator.pinValido("0123"))
        assertFalse(RutValidator.pinValido("123"))
        assertFalse(RutValidator.pinValido("12a4"))
        assertFalse(RutValidator.pinValido("12345"))
    }
}
