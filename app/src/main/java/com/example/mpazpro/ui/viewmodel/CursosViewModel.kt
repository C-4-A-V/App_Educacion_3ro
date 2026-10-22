package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.CursoRepository
import com.example.mpazpro.model.CursosUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CursosViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = CursoRepository(
        MpazDatabase.obtenerInstancia(application).cursoDao()
    )

    private val _uiState = MutableStateFlow(CursosUiState())
    val uiState = _uiState.asStateFlow()

    private var docenteActualId: Long? = null
    private var observacionJob: Job? = null

    fun cargarCursos(docenteId: Long) {
        // Evita iniciar varias observaciones para el mismo docente.
        if (docenteActualId == docenteId) return

        observacionJob?.cancel()
        docenteActualId = docenteId
        _uiState.value = CursosUiState()

        observacionJob = viewModelScope.launch {
            try {
                repository.observarCursos(docenteId).collect { cursos ->
                    _uiState.update {
                        it.copy(
                            cursos = cursos,
                            cargando = false
                        )
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensaje = "No se pudieron cargar los cursos."
                    )
                }
            }
        }
    }

    fun actualizarNombre(valor: String) {
        if (_uiState.value.guardando) return

        _uiState.update {
            it.copy(
                nombre = valor,
                errorNombre = null,
                mensaje = null
            )
        }
    }

    fun crearCurso() {
        val docenteId = docenteActualId ?: return
        val actual = _uiState.value

        if (actual.guardando || actual.cargando) return

        if (actual.nombre.isBlank()) {
            _uiState.update {
                it.copy(
                    errorNombre = "Ingresa el nombre del curso",
                    mensaje = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                guardando = true,
                errorNombre = null,
                mensaje = null
            )
        }

        viewModelScope.launch {
            try {
                val id = repository.crearCurso(
                    docenteId = docenteId,
                    nombre = actual.nombre
                )

                _uiState.update {
                    if (id == -1L) {
                        it.copy(
                            guardando = false,
                            errorNombre = "Ya tienes un curso con ese nombre"
                        )
                    } else {
                        it.copy(
                            nombre = "",
                            guardando = false,
                            mensaje = "Curso guardado"
                        )
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje = "No se pudo guardar el curso."
                    )
                }
            }
        }
    }
}
