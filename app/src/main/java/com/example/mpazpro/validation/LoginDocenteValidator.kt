package com.example.mpazpro.validation

import android.util.Patterns
import com.example.mpazpro.model.LoginDocenteErrores

object LoginDocenteValidator {

    fun validar(
        correo: String,
        clave: String
    ): LoginDocenteErrores {

        val errorCorreo = when {
            correo.isBlank() ->
                "Ingresa tu correo"

            !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() ->
                "Ingresa un correo válido"

            else -> null
        }

        // En el acceso solo comprobamos que se ingrese una contraseña.

        val errorClave = if (clave.isBlank()) {
            "Ingresa tu contraseña"
        } else {
            null
        }

        return LoginDocenteErrores(
            correo = errorCorreo,
            clave = errorClave
        )
    }
}