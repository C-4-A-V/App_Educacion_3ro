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
) {
    val totalEstudiantes: Int get() = resultados.size
    val totalRespondidas: Int get() = resultados.count { it.respondida }
    val totalCorrectas: Int get() = resultados.count { it.correcta == true }
    val totalIncorrectas: Int get() = resultados.count { it.correcta == false }
    val porcentajeAcierto: Float
        get() = if (totalRespondidas > 0) (totalCorrectas.toFloat() / totalRespondidas.toFloat()).coerceIn(0f, 1f) else 0f
    val porcentajeAciertoTexto: String
        get() = "${(porcentajeAcierto * 100).toInt()}%"
}
