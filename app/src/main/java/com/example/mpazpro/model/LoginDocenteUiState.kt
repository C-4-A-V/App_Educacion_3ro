package com.example.mpazpro.model

data class LoginDocenteErrores(
    val correo: String? = null,
    val clave: String? = null
)

data class LoginDocenteUiState(
    val correo: String = "",
    val clave: String = "",
    val errores: LoginDocenteErrores = LoginDocenteErrores(),
    val intentoValidar: Boolean = false,
    val cargando: Boolean = false,
    val docente: DocenteSesion? = null,
    val recuperacionNecesaria: Boolean = false,
    val recuperarAnterior: Boolean = false,
    val mensaje: String? = null
)
