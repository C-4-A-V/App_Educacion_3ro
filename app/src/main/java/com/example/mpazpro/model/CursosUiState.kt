package com.example.mpazpro.model

import com.example.mpazpro.data.local.CursoEntity

enum class ConexionCurso { CONECTANDO, CONECTADO, SIN_CONEXION, REVISAR }

fun resumenActualizacionCursos(estado: CursosUiState, sinConexion: Boolean): String {
    val resultado = when {
        estado.requiereRevisionGlobal || estado.conexiones.values.any { it == ConexionCurso.REVISAR } ->
            "Algunos cursos necesitan revisión antes de conectarse."
        sinConexion || estado.conexiones.values.any { it == ConexionCurso.SIN_CONEXION } ->
            "No pudimos conectar todos los cursos. Se volverá a intentar."
        else -> "Cursos actualizados."
    }
    return resultado + " Las preguntas compartidas se consultan al abrir cada asignatura. Los estudiantes, fotos y resultados del docente todavía no se sincronizan entre dispositivos."
}

data class CursosUiState(
    val nombre: String = "",
    val cursos: List<CursoEntity> = emptyList(),
    val conexiones: Map<Long, ConexionCurso> = emptyMap(),
    val remotosSinVincular: List<com.example.mpazpro.data.remote.CursoDocenteRemoto> = emptyList(),
    val localesVinculados: Set<Long> = emptySet(),
    val operacionesPendientes: Int = 0,
    val seccionesPendientesDeAsociar: Set<Char> = emptySet(),
    val requiereRevisionGlobal: Boolean = false,
    val actualizandoManual: Boolean = false,
    val avisoActualizacion: String? = null,
    val errorNombre: String? = null,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val mensaje: String? = null
)
