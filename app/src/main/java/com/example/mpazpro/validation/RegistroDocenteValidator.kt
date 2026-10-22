package com.example.mpazpro.validation

import android.util.Patterns
import com.example.mpazpro.model.RegistroDocenteErrores
import com.example.mpazpro.model.RegistroDocenteUiState

object RegistroDocenteValidator {

    fun validar(estado: RegistroDocenteUiState): RegistroDocenteErrores {
        return RegistroDocenteErrores(
            nombre = when {
                estado.nombre.isBlank() -> "Ingresa tu nombre"
                estado.nombre.trim().length < 2 ->
                    "Escribe al menos 2 caracteres"
                else -> null
            },
            correo = when {
                estado.correo.isBlank() -> "Ingresa tu correo"
                !Patterns.EMAIL_ADDRESS
                    .matcher(estado.correo.trim()).matches() ->
                    "Ingresa un correo válido"
                else -> null
            },
            clave = when {
                estado.clave.isBlank() -> "Ingresa una contraseña"
                estado.clave.length < 8 -> "Usa al menos 8 caracteres"
                else -> null
            },
            confirmacion = when {
                estado.confirmacion.isBlank() ->
                    "Repite la contraseña"
                estado.confirmacion != estado.clave ->
                    "Las contraseñas no coinciden"
                else -> null
            }
        )
    }
}
