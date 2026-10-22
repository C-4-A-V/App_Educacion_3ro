package com.example.mpazpro.model

import com.example.mpazpro.data.local.CursoEntity

data class CursosUiState(
    val nombre: String = "",
    val cursos: List<CursoEntity> = emptyList(),
    val errorNombre: String? = null,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val mensaje: String? = null
)
