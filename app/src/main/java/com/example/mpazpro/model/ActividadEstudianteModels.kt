package com.example.mpazpro.model

import com.example.mpazpro.data.local.IntentoActividadEntity

data class ActividadEstudianteResumen(
    val id: Long,
    val titulo: String,
    val instrucciones: String,
    val resuelta: Boolean
)

data class ActividadParaResolver(
    val id: Long,
    val asignaturaId: String,
    val titulo: String,
    val instrucciones: String,
    val pregunta: String,
    val alternativaA: String,
    val alternativaB: String,
    val alternativaC: String,
    val alternativaD: String
)

data class ActividadesEstudianteUiState(
    val actividades: List<ActividadEstudianteResumen> = emptyList(),
    val cargando: Boolean = true,
    val mensaje: String? = null
)

data class ResolverActividadUiState(
    val actividad: ActividadParaResolver? = null,
    val respuestaSeleccionada: Int = -1,
    val intento: IntentoActividadEntity? = null,
    val cargando: Boolean = true,
    val enviando: Boolean = false,
    val mensaje: String? = null
)
