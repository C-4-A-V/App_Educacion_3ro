package com.example.mpazpro.model

import com.example.mpazpro.data.local.ActividadEntity
import com.example.mpazpro.data.local.CursoEntity
import com.example.mpazpro.validation.ActividadErrores

data class CursoDestinoActividad(val id: Long, val nombre: String, val vinculado: Boolean)

enum class EstadoPublicacionDocente { SOLO_LOCAL, PENDIENTE, COMPARTIDA, OTRO_VINCULO }

data class ActividadesDocenteUiState(
    val curso: CursoEntity? = null,
    val actividades: List<ActividadEntity> = emptyList(),
    val publicaciones: Map<Long, EstadoPublicacionDocente> = emptyMap(),
    val preguntasServidor: List<com.example.mpazpro.data.remote.ActividadRemotaEstudiante> = emptyList(),
    val idsPublicadosLocales: Set<Long> = emptySet(),
    val servidorPorActividad: Map<Long, Long> = emptyMap(),
    val consultandoServidor: Boolean = false,
    val consultaServidorRealizada: Boolean = false,
    val errorServidor: Boolean = false,
    val cursoVinculado: Boolean = false,
    val cursosDestino: List<CursoDestinoActividad> = emptyList(),
    val enviandoOtroCurso: Boolean = false,
    val publicacionDisponible: Boolean = false,
    val publicandoId: Long? = null,
    val preparandoBanco: Boolean = false,
    val compartiendoPendientes: Boolean = false,
    val retirando: Boolean = false,
    val avisoPublicacion: String? = null,
    val cargando: Boolean = true,
    val mensaje: String? = null
)

data class CrearActividadUiState(
    val curso: CursoEntity? = null,
    val formulario: ActividadFormulario = ActividadFormulario(),
    val errores: ActividadErrores = ActividadErrores(),
    val intentoValidar: Boolean = false,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val publicacionPendiente: Boolean = false,
    val actividadGuardadaId: Long? = null,
    val mensaje: String? = null
)