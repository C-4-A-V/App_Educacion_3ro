package com.example.mpazpro.model

data class ActividadFormulario(
    val titulo: String = "",
    val instrucciones: String = "",
    val pregunta: String = "",
    val alternativas: List<String> = listOf("", "", "", ""),
    val respuestaCorrecta: Int = -1,
    val maxIntentos: Int = 3,
    val explicacion: String = ""
) {
    fun normalizado(): ActividadFormulario {
        return copy(
            titulo = titulo.trim(),
            explicacion = explicacion.trim(),
            instrucciones = instrucciones.trim(),
            pregunta = pregunta.trim(),
            alternativas = alternativas.map {
                it.trim().replace(Regex("\\s+"), " ")
            }
        )
    }
}