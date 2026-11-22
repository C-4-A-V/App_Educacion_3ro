package com.example.mpazpro.model

import com.example.mpazpro.data.local.CursoEntity

data class CursoDocenteUiState(
    val curso: CursoEntity? = null,
    val cargando: Boolean = true,
    val vinculado: Boolean = false,
    val preparandoBanco: Boolean = false,
    val avisoBanco: String? = null,
    val mensaje: String? = null
)