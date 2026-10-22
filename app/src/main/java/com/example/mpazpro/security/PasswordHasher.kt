package com.example.mpazpro.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Datos que guardaremos en lugar de la contraseña original.
data class ClaveProtegida(
    val hash: String,
    val salt: String,
    val iteraciones: Int
)

object PasswordHasher {

    private const val ITERACIONES = 600_000
    private const val LONGITUD_BITS = 256

    fun generar(clave: String): ClaveProtegida {
        // Cada cuenta recibe un salt aleatorio diferente.
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val hash = calcular(clave, salt, ITERACIONES)

        return ClaveProtegida(
            hash = Base64.encodeToString(hash, Base64.NO_WRAP),
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            iteraciones = ITERACIONES
        )
    }

    fun verificar(
        clave: String,
        hashGuardado: String,
        saltGuardado: String,
        iteraciones: Int
    ): Boolean {
        val salt = Base64.decode(saltGuardado, Base64.NO_WRAP)
        val hashEsperado = Base64.decode(hashGuardado, Base64.NO_WRAP)

        val hashCalculado = calcular(clave, salt, iteraciones)

        return MessageDigest.isEqual(hashEsperado, hashCalculado)
    }

    private fun calcular(
        clave: String,
        salt: ByteArray,
        iteraciones: Int
    ): ByteArray {
        val caracteres = clave.toCharArray()

        try {
            val especificacion = PBEKeySpec(
                caracteres,
                salt,
                iteraciones,
                LONGITUD_BITS
            )

            try {
                return SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion)
                    .encoded
            } finally {
                especificacion.clearPassword()
            }
        } finally {
            caracteres.fill('\u0000')
        }
    }
}
