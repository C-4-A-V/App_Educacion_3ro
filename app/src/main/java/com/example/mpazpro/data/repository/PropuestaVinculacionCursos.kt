package com.example.mpazpro.data.repository

import java.util.Locale

data class CursoParaVincular(val id: Long, val nombre: String)
data class PropuestaCurso(val cursoServidorId: Long, val cursoLocalId: Long)
data class ResultadoPropuestasCursos(
    val propuestas: List<PropuestaCurso>,
    val seccionesAmbiguas: Set<Char>
)

/** Solo propone coincidencias; no modifica cursos ni vincula identificadores temporales. */
object PropuestaVinculacionCursos {
    fun seccion(nombre: String): Char? {
        val normalizado = nombre.trim().uppercase(Locale.ROOT)
        val coincidencia = Regex("^3\\s*(?:[.º°]+)?\\s*(?:B[ÁA]SICO\\s+)?([A-Z])$").matchEntire(normalizado)
        return coincidencia?.groupValues?.get(1)?.singleOrNull()
    }

    fun nombreVisible(nombre: String): String =
        seccion(nombre)?.let { "3.º Básico $it" } ?: nombre

    /** No supone la sección de nombres desconocidos ni de cursos duplicados. */
    fun siguiente(nombreCursos: List<String>): Char? {
        val secciones = nombreCursos.map { seccion(it) ?: return null }
        if (secciones.distinct().size != secciones.size) return null
        return ('A'..'Z').firstOrNull { it !in secciones }
    }

    fun comparar(locales: List<CursoParaVincular>, servidor: List<CursoParaVincular>): ResultadoPropuestasCursos {
        val localesPorSeccion = locales.mapNotNull { curso -> seccion(curso.nombre)?.let { it to curso } }.groupBy({ it.first }, { it.second })
        val servidorPorSeccion = servidor.mapNotNull { curso -> seccion(curso.nombre)?.let { it to curso } }.groupBy({ it.first }, { it.second })
        val ambiguas = (localesPorSeccion.keys intersect servidorPorSeccion.keys).filterTo(mutableSetOf()) {
            localesPorSeccion.getValue(it).size != 1 || servidorPorSeccion.getValue(it).size != 1
        }
        val propuestas = (localesPorSeccion.keys intersect servidorPorSeccion.keys)
            .filterNot { it in ambiguas }
            .sorted()
            .map { seccion ->
                PropuestaCurso(servidorPorSeccion.getValue(seccion).single().id, localesPorSeccion.getValue(seccion).single().id)
            }
        return ResultadoPropuestasCursos(propuestas, ambiguas)
    }
}
