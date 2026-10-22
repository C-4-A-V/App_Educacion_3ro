package com.example.mpazpro.model

enum class Asignatura(
    val id: String,
    val titulo: String,
    val descripcion: String
) {
    LENGUAJE(
        id = "lenguaje",
        titulo = "Lenguaje y Comunicación",
        descripcion = "Lectura, escritura y comprensión de textos."
    ),

    MATEMATICA(
        id = "matematica",
        titulo = "Matemática",
        descripcion = "Números, operaciones y resolución de problemas."
    ),

    CIENCIAS(
        id = "ciencias",
        titulo = "Ciencias Naturales",
        descripcion = "Exploremos los seres vivos y nuestro entorno."
    ),

    HISTORIA(
        id = "historia",
        titulo = "Historia, Geografía y Ciencias Sociales",
        descripcion = "Conozcamos nuestra comunidad, historia y territorio."
    );

    companion object {
        fun buscarPorId(id: String?): Asignatura? {
            return entries.find { it.id == id }
        }
    }
}