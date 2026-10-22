package com.example.mpazpro.model

data class RegistroDocenteErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val confirmacion: String? = null
)

data class RegistroDocenteUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmacion: String = "",
    val errores: RegistroDocenteErrores = RegistroDocenteErrores(),
    val intentoValidar: Boolean = false,
    val guardando: Boolean = false,
    val registrado: Boolean = false,
    val mensaje: String? = null
)
