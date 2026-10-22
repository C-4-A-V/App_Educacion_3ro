package com.example.mpazpro.model

import com.example.mpazpro.data.local.CursoEntity

data class CursoDocenteUiState(
    val curso: CursoEntity? = null,
    val cargando: Boolean = true,
    val mensaje: String? = null
)