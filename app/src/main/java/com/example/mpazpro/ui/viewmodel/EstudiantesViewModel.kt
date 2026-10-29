package com.example.mpazpro.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.media.FotoStorage
import com.example.mpazpro.data.repository.EstudianteRepository
import com.example.mpazpro.model.EstudiantesUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EstudiantesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)

    private val repository = EstudianteRepository(
        estudianteDao = database.estudianteDao(),
        cursoDao = database.cursoDao(),
        actividadDao = database.actividadDao()
    )

    private val fotoStorage = FotoStorage(application)

    private val _uiState = MutableStateFlow(EstudiantesUiState())
    val uiState = _uiState.asStateFlow()

    private var contextoActual: Pair<Long, Long>? = null
    private var observacionJob: Job? = null

    fun cargar(cursoId: Long, docenteId: Long) {
        val nuevoContexto = cursoId to docenteId

        if (contextoActual == nuevoContexto) return

        observacionJob?.cancel()
        contextoActual = nuevoContexto
        _uiState.value = EstudiantesUiState()

        observacionJob = viewModelScope.launch {
            try {
                val curso = repository.obtenerCurso(cursoId, docenteId)

                if (curso == null) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensaje = "El curso no está disponible."
                        )
                    }
                    return@launch
                }

                _uiState.update { it.copy(curso = curso) }

                repository.observarEstudiantes(cursoId, docenteId)
                    .collect { estudiantes ->
                        _uiState.update {
                            it.copy(
                                estudiantes = estudiantes,
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
                        mensaje = "No se pudieron cargar los estudiantes."
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

    fun seleccionarFoto(uri: Uri) {
        val actual = _uiState.value
        if (actual.guardando || actual.importandoFoto) return

        _uiState.update {
            it.copy(
                importandoFoto = true,
                errorFoto = null,
                mensaje = null
            )
        }

        viewModelScope.launch {
            try {
                val ruta = fotoStorage.importar(uri)

                _uiState.update {
                    it.copy(
                        fotoRuta = ruta,
                        importandoFoto = false,
                        errorFoto = null
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        importandoFoto = false,
                        errorFoto =
                            "No se pudo cargar la foto. Usa una imagen de hasta 10 MB."
                    )
                }
            }
        }
    }

    fun mostrarErrorFoto(mensaje: String) {
        _uiState.update { it.copy(errorFoto = mensaje) }
    }

    fun registrar() {
        val actual = _uiState.value
        val curso = actual.curso ?: return
        val docenteId = contextoActual?.second ?: return

        if (
            actual.guardando ||
            actual.cargando ||
            actual.importandoFoto
        ) return

        val nombreLimpio = actual.nombre
            .trim()
            .replace(Regex("\\s+"), " ")

        val errorNombre = when {
            nombreLimpio.isBlank() -> "Ingresa el nombre"
            nombreLimpio.length < 2 -> "Escribe al menos 2 caracteres"
            else -> null
        }

        val foto = actual.fotoRuta

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorFoto = if (foto == null) {
                    "Toma o selecciona una fotografía"
                } else {
                    null
                },
                mensaje = null
            )
        }

        if (errorNombre != null || foto == null) return

        _uiState.update { it.copy(guardando = true) }

        viewModelScope.launch {
            try {
                repository.registrar(
                    cursoId = curso.id,
                    docenteId = docenteId,
                    nombre = nombreLimpio,
                    fotoRuta = foto
                )

                _uiState.update {
                    it.copy(
                        nombre = "",
                        fotoRuta = null,
                        errorFoto = null,
                        guardando = false,
                        mensaje = "Estudiante y fotografía guardados"
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje = "No se pudo guardar el estudiante."
                    )
                }
            }
        }
    }
}
