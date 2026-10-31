package com.example.mpazpro.model

import com.example.mpazpro.data.local.CursoEntity

data class AvanceAlumnoFila(
    val estudianteId: Long,
    val nombre: String,
    val fotoRuta: String?,
    val cursoId: Long,
    val asignaturaId: String?,
    val total: Int,
    val completadas: Int,
    val correctas: Int
)

data class ResumenAlumno(
    val id: Long,
    val nombre: String,
    val fotoRuta: String?,
    val cursoId: Long,
    val materias: Map<String, ProgresoEstudiante>
) {
    val general: ProgresoEstudiante
        get() = ProgresoEstudiante(
            total = materias.values.sumOf { it.total },
            completadas = materias.values.sumOf { it.completadas },
            correctas = materias.values.sumOf { it.correctas }
        )
}

data class ResumenDocenteUiState(
    val cursos: List<CursoEntity> = emptyList(),
    val alumnos: List<ResumenAlumno> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)
