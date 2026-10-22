package com.example.mpazpro.model

// Información que necesita el selector de estudiantes.
data class PerfilEstudiante(
    val id: Long,
    val nombre: String,
    val cursoNombre: String,
    val fotoRuta: String?
)

data class AccesoEstudianteUiState(
    val perfiles: List<PerfilEstudiante> = emptyList(),
    val seleccionado: PerfilEstudiante? = null,
    val cargando: Boolean = true,
    val mensaje: String? = null
)
