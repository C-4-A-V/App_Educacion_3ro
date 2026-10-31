package com.example.mpazpro.validation

import com.example.mpazpro.model.ActividadFormulario
import java.util.Locale

data class ActividadErrores(
    val titulo: String? = null,
    val instrucciones: String? = null,
    val pregunta: String? = null,
    val explicacion: String? = null,
    val alternativas: List<String?> = List(4) { null },
    val respuestaCorrecta: String? = null,
    val modalidad: String? = null
) {
    val primerError: String?
        get() = listOf(titulo, instrucciones, pregunta, explicacion, modalidad)
            .plus(alternativas).plus(respuestaCorrecta).firstOrNull { it != null }
    val hayErrores: Boolean get() = primerError != null
}

object ActividadValidator {
    fun validar(formulario: ActividadFormulario): String? = errores(formulario).primerError

    fun errores(formulario: ActividadFormulario): ActividadErrores {
        val datos = formulario.normalizado()
        val textos = datos.alternativas.map { it.lowercase(Locale.ROOT) }
        return ActividadErrores(
            titulo = if (datos.titulo.length !in 3..80) "Usa entre 3 y 80 caracteres." else null,
            instrucciones = if (datos.instrucciones.length !in 1..1000) "Escribe instrucciones de hasta 1000 caracteres." else null,
            pregunta = if (datos.pregunta.length !in 5..500) "Usa entre 5 y 500 caracteres." else null,
            explicacion = if (datos.explicacion.length !in 1..1000) "Escribe una explicación de hasta 1000 caracteres." else null,
            alternativas = List(4) { indice ->
                val texto = datos.alternativas.getOrNull(indice)
                when {
                    texto.isNullOrBlank() -> "Completa esta alternativa."
                    texto.length > 150 -> "Usa hasta 150 caracteres."
                    textos.count { it == texto.lowercase(Locale.ROOT) } > 1 -> "Las alternativas deben ser diferentes."
                    else -> null
                }
            },
            respuestaCorrecta = if (datos.respuestaCorrecta !in 0..3) "Marca la alternativa correcta." else null,
            modalidad = when {
                datos.maxIntentos !in listOf(1, 3) -> "Selecciona práctica o evaluación."
                datos.alternativas.size != 4 -> "La actividad debe tener cuatro alternativas."
                else -> null
            }
        )
    }
}
