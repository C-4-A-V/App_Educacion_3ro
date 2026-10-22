package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.ActividadRepository
import com.example.mpazpro.data.repository.CursoRepository
import com.example.mpazpro.model.ActividadFormulario
import com.example.mpazpro.model.CrearActividadUiState
import com.example.mpazpro.validation.ActividadValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CrearActividadViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)

    private val cursos = CursoRepository(database.cursoDao())
    private val actividades = ActividadRepository(database)

    private val _uiState = MutableStateFlow(
        CrearActividadUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var contexto: Pair<Long, Long>? = null
    private var cargaJob: Job? = null

    fun cargar(
        cursoId: Long,
        docenteId: Long
    ) {
        val nuevoContexto = cursoId to docenteId
        if (contexto == nuevoContexto) return

        cargaJob?.cancel()
        contexto = nuevoContexto
        _uiState.value = CrearActividadUiState()

        cargaJob = viewModelScope.launch {
            try {
                val curso = cursos.obtenerCurso(
                    cursoId = cursoId,
                    docenteId = docenteId
                )

                _uiState.update {
                    it.copy(
                        curso = curso,
                        cargando = false,
                        mensaje = if (curso == null) {
                            "Curso no disponible."
                        } else {
                            null
                        }
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensaje = "No se pudo cargar el curso."
                    )
                }
            }
        }
    }

    private fun editar(
        cambio: (ActividadFormulario) -> ActividadFormulario
    ) {
        val actual = _uiState.value

        if (actual.guardando || actual.guardado) return

        _uiState.update {
            it.copy(
                formulario = cambio(it.formulario),
                mensaje = null
            )
        }
    }

    fun actualizarTitulo(valor: String) {
        editar { it.copy(titulo = valor) }
    }

    fun actualizarInstrucciones(valor: String) {
        editar { it.copy(instrucciones = valor) }
    }

    fun actualizarPregunta(valor: String) {
        editar { it.copy(pregunta = valor) }
    }

    fun actualizarAlternativa(
        indice: Int,
        valor: String
    ) {
        if (indice !in 0..3) return

        editar { formulario ->
            formulario.copy(
                alternativas = formulario.alternativas.mapIndexed {
                        posicion, texto ->
                    if (posicion == indice) valor else texto
                }
            )
        }
    }

    fun seleccionarRespuesta(indice: Int) {
        if (indice !in 0..3) return

        editar {
            it.copy(respuestaCorrecta = indice)
        }
    }

    fun guardar(asignaturaId: String) {
        val actual = _uiState.value
        val curso = actual.curso ?: return
        val docenteId = contexto?.second ?: return

        if (
            actual.cargando ||
            actual.guardando ||
            actual.guardado
        ) return

        val error = ActividadValidator.validar(
            actual.formulario
        )

        if (error != null) {
            _uiState.update {
                it.copy(mensaje = error)
            }
            return
        }

        _uiState.update {
            it.copy(
                guardando = true,
                mensaje = null
            )
        }

        viewModelScope.launch {
            try {
                actividades.crearActividad(
                    cursoId = curso.id,
                    asignaturaId = asignaturaId,
                    docenteId = docenteId,
                    formulario = actual.formulario
                )

                _uiState.update {
                    it.copy(
                        guardando = false,
                        guardado = true
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje = "No se pudo guardar la actividad."
                    )
                }
            }
        }
    }
}