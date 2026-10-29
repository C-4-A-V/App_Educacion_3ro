package com.example.mpazpro.model

import com.example.mpazpro.data.local.ActividadEntity

data class EstudianteResultadoItem(
    val estudianteId: Long,
    val estudianteNombre: String,
    val estudianteFoto: String?,
    val respondida: Boolean,
    val correcta: Boolean?,
    val respuestaElegida: Int?
)

data class ResultadosActividadUiState(
    val actividad: ActividadEntity? = null,
    val resultados: List<EstudianteResultadoItem> = emptyList(),
    val cargando: Boolean = true,
    val mensaje: String? = null
)
