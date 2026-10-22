package com.example.mpazpro.model

import com.example.mpazpro.data.local.ActividadEntity
import com.example.mpazpro.data.local.CursoEntity

data class ActividadesDocenteUiState(
    val curso: CursoEntity? = null,
    val actividades: List<ActividadEntity> = emptyList(),
    val cargando: Boolean = true,
    val mensaje: String? = null
)

data class CrearActividadUiState(
    val curso: CursoEntity? = null,
    val formulario: ActividadFormulario = ActividadFormulario(),
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val mensaje: String? = null
)