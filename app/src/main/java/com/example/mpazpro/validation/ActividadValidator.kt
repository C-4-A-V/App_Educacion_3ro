package com.example.mpazpro.validation

import com.example.mpazpro.model.ActividadFormulario
import java.util.Locale

object ActividadValidator {

    fun validar(formulario: ActividadFormulario): String? {
        val datos = formulario.normalizado()

        return when {
            datos.titulo.length !in 3..80 ->
                "El título debe tener entre 3 y 80 caracteres."

            datos.instrucciones.length !in 1..1000 ->
                "Escribe instrucciones de hasta 1000 caracteres."

            datos.pregunta.length !in 5..500 ->
                "La pregunta debe tener entre 5 y 500 caracteres."

            datos.alternativas.size != 4 ->
                "La actividad debe tener cuatro alternativas."

            datos.alternativas.any { it.isBlank() } ->
                "Completa las cuatro alternativas."

            datos.alternativas.any { it.length > 150 } ->
                "Cada alternativa admite hasta 150 caracteres."

            datos.alternativas
                .map { it.lowercase(Locale.ROOT) }
                .distinct()
                .size != 4 ->
                "Las alternativas deben ser diferentes."

            datos.respuestaCorrecta !in 0..3 ->
                "Selecciona la respuesta correcta."

            else -> null
        }
    }
}