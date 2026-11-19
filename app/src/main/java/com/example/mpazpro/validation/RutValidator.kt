package com.example.mpazpro.validation

object RutValidator {
    fun normalizar(valor: String): String? {
        val limpio = valor.trim().uppercase().replace(".", "").replace("-", "").replace(" ", "")
        if (limpio.length !in 8..9 || limpio.dropLast(1).any { it !in '0'..'9' } ||
            limpio.last() !in "0123456789K" || limpio.first() == '0') return null
        val cuerpo = limpio.dropLast(1)
        var factor = 2
        var suma = 0
        cuerpo.reversed().forEach {
            suma += (it - '0') * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        val resto = 11 - suma % 11
        val digito = when (resto) { 11 -> '0'; 10 -> 'K'; else -> ('0' + resto) }
        return limpio.takeIf { it.last() == digito }
    }

    fun pinValido(pin: String) = pin.length == 4 && pin.all { it in '0'..'9' }
}
