package com.example.mpazpro.model

import com.example.mpazpro.data.local.CursoEntity

// Información que necesita el selector de estudiantes.
data class PerfilEstudiante(
    val id: Long,
    val nombre: String,
    val cursoNombre: String,
    val fotoRuta: String?
)

data class AccesoEstudianteUiState(
    val perfiles: List<PerfilEstudiante> = emptyList(),
    val cursos: List<CursoEntity> = emptyList(),
    val cursoId: Long? = null,
    val clave: String = "",
    val seleccionado: PerfilEstudiante? = null,
    val cargando: Boolean = true,
    val ingresando: Boolean = false,
    val mensaje: String? = null,
    val progreso: ProgresoEstudiante = ProgresoEstudiante()
)
