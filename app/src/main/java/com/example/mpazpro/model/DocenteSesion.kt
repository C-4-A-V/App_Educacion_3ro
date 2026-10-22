package com.example.mpazpro.model

// Solo contiene los datos que necesita la interfaz.
// No expone el hash ni el salt de la contraseña.
data class DocenteSesion(
    val id: Long,
    val nombre: String,
    val correo: String
)
