package com.example.mpazpro.model

import com.example.mpazpro.data.local.CursoEntity
import com.example.mpazpro.data.local.EstudianteEntity

data class EstudiantesUiState(
    val curso: CursoEntity? = null,
    val estudiantes: List<EstudianteEntity> = emptyList(),
    val nombre: String = "",
    val rut: String = "",
    val pin: String = "",
    val editandoAccesoId: Long? = null,
    val errorAcceso: String? = null,
    val errorNombre: String? = null,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val fotoRuta: String? = null,
    val errorFoto: String? = null,
    val importandoFoto: Boolean = false
)
