package com.example.mpazpro.data.repository

import com.example.mpazpro.data.remote.PaisesApi
import com.example.mpazpro.data.remote.ServicioPaises

enum class TemaPudu(val etiqueta: String) {
    TODOS("Todos"), CHILE("Chile"), HISTORIA("Historia"), NATURALEZA("Naturaleza"),
    ESPACIO("Espacio"), ANIMALES("Animales"), LENGUAJE("Lenguaje"), MATEMATICA("Matemática")
}

data class DatoPudu(
    val id: String,
    val tema: TemaPudu,
    val texto: String,
    val fuente: String,
    val enlace: String
)

class AprendeRepository(private val api: PaisesApi = ServicioPaises.api) {
    val datosLocales = listOf(
        DatoPudu("junta", TemaPudu.HISTORIA, "El 18 de septiembre de 1810 se formó la Primera Junta de Gobierno en Santiago.", "Memoria Chilena", "https://www.memoriachilena.gob.cl/602/w3-article-91935.html"),
        DatoPudu("independencia", TemaPudu.HISTORIA, "La independencia de Chile se proclamó el 12 de febrero de 1818.", "Memoria Chilena", "https://www.memoriachilena.gob.cl/602/w3-article-96486.html"),
        DatoPudu("aurora", TemaPudu.HISTORIA, "La Aurora de Chile fue el primer periódico chileno. Empezó a circular en 1812.", "Memoria Chilena", "https://www.memoriachilena.gob.cl/602/w3-article-3532.html"),
        DatoPudu("mistral", TemaPudu.LENGUAJE, "La poeta chilena Gabriela Mistral recibió el Premio Nobel de Literatura en 1945.", "Memoria Chilena", "https://www.memoriachilena.gob.cl/602/w3-article-3429.html"),
        DatoPudu("fabula", TemaPudu.LENGUAJE, "Las fábulas pueden dejarnos una enseñanza llamada moraleja. ¿Conoces alguna?", "Currículum Nacional", "https://www.curriculumnacional.cl/estudiantes/Educacion-General/Lenguaje-y-comunicacion-3-basico/LE03-OA-01/23544%3AEl-leon-y-el-raton"),
        DatoPudu("ronda", TemaPudu.LENGUAJE, "Gabriela Mistral escribió poemas y también rondas para compartir palabras y juegos.", "Memoria Chilena", "https://www.memoriachilena.gob.cl/602/w3-article-3429.html"),
        DatoPudu("mitades", TemaPudu.MATEMATICA, "Si dividimos una pizza en dos partes iguales, cada parte es una mitad.", "Currículum Nacional", "https://www.curriculumnacional.cl/614/articles-18978_programa.pdf"),
        DatoPudu("multiplicar", TemaPudu.MATEMATICA, "Multiplicar puede ayudarnos a contar grupos iguales: 3 grupos de 2 son 6.", "Currículum Nacional", "https://www.curriculumnacional.cl/614/articles-18978_programa.pdf"),
        DatoPudu("cuartos", TemaPudu.MATEMATICA, "Si dividimos una figura en cuatro partes iguales, cada parte es un cuarto.", "Currículum Nacional", "https://www.curriculumnacional.cl/614/articles-18978_programa.pdf"),
        DatoPudu("huemul", TemaPudu.ANIMALES, "El huemul es un ciervo que vive en los Andes y necesita nuestra protección.", "CONAF", "https://www.conaf.cl/parque_nacionales/reserva-nacional-huemules-del-niblinto/"),
        DatoPudu("vicuña", TemaPudu.CHILE, "Gabriela Mistral nació en Vicuña, una ciudad del valle de Elqui, en Chile.", "Memoria Chilena", "https://www.memoriachilena.gob.cl/602/w3-article-3429.html"),
        DatoPudu("regiones", TemaPudu.CHILE, "¿Sabías que Chile tiene 16 regiones? ¡Cada una tiene lugares especiales!", "Biblioteca del Congreso Nacional", "https://www.bcn.cl/formacioncivica/detalle_guia?h=10221.3%2F45764"),
        DatoPudu("agua", TemaPudu.NATURALEZA, "El calor del Sol ayuda a que el agua se transforme en vapor. ¡Es parte del ciclo del agua!", "Servicio Geológico de Estados Unidos", "https://www.usgs.gov/special-topics/water-science-school/water-cycle"),
        DatoPudu("lluvia", TemaPudu.NATURALEZA, "El agua viaja entre la Tierra y la atmósfera. ¡La lluvia es parte de ese viaje!", "Servicio Geológico de Estados Unidos", "https://www.usgs.gov/special-topics/water-science-school/water-cycle"),
        DatoPudu("luna", TemaPudu.ESPACIO, "¿Sabías que la Luna brilla porque refleja la luz del Sol?", "NASA", "https://science.nasa.gov/moon/moonlight/"),
        DatoPudu("orbita", TemaPudu.ESPACIO, "La Luna es el satélite natural de la Tierra y viaja a su alrededor.", "NASA", "https://science.nasa.gov/moon/top-moon-questions/"),
        DatoPudu("pudu", TemaPudu.ANIMALES, "¡El pudú es un ciervo muy pequeño que vive en el sur de Chile!", "CONAF", "https://www.conaf.cl/camaras-trampa-detectaron-pudu-y-zorro-chilote-en-el-parque-nacional-chiloe/"),
        DatoPudu("chiloe", TemaPudu.ANIMALES, "En la isla de Chiloé viven pudúes. ¡Como nuestra mascota exploradora!", "CONAF", "https://www.conaf.cl/camaras-trampa-detectaron-pudu-y-zorro-chilote-en-el-parque-nacional-chiloe/")
    )

    suspend fun capitalDeChile(): DatoPudu {
        val respuesta = api.obtenerChile()
        val pais = respuesta.elementAtOrNull(1)?.takeIf { it.isJsonArray }
            ?.asJsonArray?.firstOrNull()?.takeIf { it.isJsonObject }?.asJsonObject
            ?: error("Respuesta incompleta")
        check(pais.get("id")?.asString == "CHL")
        val capital = pais.get("capitalCity")?.asString?.trim().orEmpty()
        check(capital.isNotEmpty() && capital.length <= 50 && capital.all { it.isLetter() || it == ' ' || it == '-' })
        return DatoPudu("capital", TemaPudu.CHILE, "¿Sabías que la capital de Chile es $capital?", "Banco Mundial", "https://data.worldbank.org/country/chile")
    }
}
