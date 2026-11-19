package com.example.mpazpro.model

data class ResultadoPreguntaDocente(
    val actividadId: Long,
    val cursoId: Long,
    val estudianteId: Long,
    val asignaturaId: String,
    val titulo: String,
    val pregunta: String,
    val maxIntentos: Int,
    val intentosUsados: Int,
    val finalizada: Boolean,
    val correcta: Boolean,
    val respuestaElegida: Int,
    val respuestaCorrecta: Int,
    val tuvoError: Boolean
)

data class EstadisticaPregunta(
    val respondieron: Int,
    val correctas: Int,
    val terminaronSinAcertar: Int,
    val tuvieronErrores: Int,
    val enCurso: Int
)

fun estadisticaPregunta(resultados: List<ResultadoPreguntaDocente>) = EstadisticaPregunta(
    respondieron = resultados.size,
    correctas = resultados.count { it.finalizada && it.correcta },
    terminaronSinAcertar = resultados.count { it.finalizada && !it.correcta },
    tuvieronErrores = resultados.count { it.tuvoError },
    enCurso = resultados.count { !it.finalizada }
)

data class MensajeEstudiante(
    val id: Long,
    val contenido: String,
    val creadaEn: Long,
    val leido: Boolean,
    val docenteNombre: String,
    val actividadTitulo: String?
)
