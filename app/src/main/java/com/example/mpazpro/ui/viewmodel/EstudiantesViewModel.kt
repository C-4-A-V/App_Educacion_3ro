package com.example.mpazpro.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.media.FotoStorage
import com.example.mpazpro.data.repository.EstudianteRepository
import com.example.mpazpro.model.EstudiantesUiState
import com.example.mpazpro.validation.RutValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
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

                combine(
                    repository.observarEstudiantes(cursoId, docenteId),
                    repository.observarRetirados(cursoId, docenteId)
                ) { estudiantes, retirados -> estudiantes to retirados }
                    .collect { (estudiantes, retirados) ->
                        _uiState.update { it.copy(estudiantes = estudiantes, retirados = retirados, cargando = false) }
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

    fun retirarEstudiantes(ids: Set<Long>, alTerminar: () -> Unit) {
        val actual = contextoActual ?: return
        val estado = _uiState.value
        if (estado.guardando || estado.importandoFoto || ids.isEmpty() ||
            !estado.estudiantes.map { it.id }.containsAll(ids)) return
        _uiState.update { it.copy(guardando = true, mensaje = null) }
        viewModelScope.launch {
            try {
                repository.retirarEstudiantes(ids.toList(), actual.first, actual.second)
                if (contextoActual == actual) {
                    _uiState.update { it.copy(mensaje = "Estudiantes retirados: ${ids.size}. Puedes restaurarlos cuando quieras.") }
                    alTerminar()
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (contextoActual == actual) _uiState.update { it.copy(mensaje = "No se pudieron retirar los estudiantes. Vuelve a intentarlo.") }
            } finally {
                if (contextoActual == actual) _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    fun restaurarEstudiante(id: Long) {
        val actual = contextoActual ?: return
        val estado = _uiState.value
        if (estado.guardando || estado.importandoFoto || estado.retirados.none { it.id == id }) return
        _uiState.update { it.copy(guardando = true, mensaje = null) }
        viewModelScope.launch {
            try {
                repository.restaurarEstudiante(id, actual.first, actual.second)
                if (contextoActual == actual) _uiState.update { it.copy(mensaje = "Estudiante restaurado con su perfil y avances.") }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (contextoActual == actual) _uiState.update { it.copy(mensaje = "No se pudo restaurar al estudiante.") }
            } finally {
                if (contextoActual == actual) _uiState.update { it.copy(guardando = false) }
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

    fun actualizarRut(valor: String) { _uiState.update { it.copy(rut = valor.take(12), errorAcceso = null) } }
    fun actualizarPin(valor: String) { _uiState.update { it.copy(pin = valor.filter(Char::isDigit).take(4), errorAcceso = null) } }

    fun editarAcceso(id: Long) {
        val estudiante = _uiState.value.estudiantes.find { it.id == id } ?: return
        _uiState.update { it.copy(editandoAccesoId = id, rut = estudiante.rut.orEmpty(), pin = "", errorAcceso = null) }
    }

    fun cancelarAcceso() { _uiState.update { it.copy(editandoAccesoId = null, rut = "", pin = "", errorAcceso = null) } }

    fun guardarAcceso() {
        val actual = _uiState.value
        val id = actual.editandoAccesoId ?: return
        val curso = actual.curso ?: return
        val docenteId = contextoActual?.second ?: return
        if (actual.guardando) return
        if (RutValidator.normalizar(actual.rut) == null || !RutValidator.pinValido(actual.pin)) {
            _uiState.update { it.copy(errorAcceso = "Ingresa un RUT válido y un PIN de 4 números.") }
            return
        }
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                repository.configurarAcceso(id, curso.id, docenteId, actual.rut, actual.pin)
                _uiState.update { it.copy(guardando = false, editandoAccesoId = null, rut = "", pin = "", mensaje = "Acceso del estudiante actualizado.") }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                _uiState.update { it.copy(guardando = false, errorAcceso = "No se pudo guardar. Revisa que el RUT no esté registrado.") }
            }
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

        if (RutValidator.normalizar(actual.rut) == null || !RutValidator.pinValido(actual.pin)) {
            _uiState.update { it.copy(errorAcceso = "Ingresa un RUT válido y un PIN de 4 números.") }
            return
        }
        if (errorNombre != null || foto == null) return

        _uiState.update { it.copy(guardando = true) }

        viewModelScope.launch {
            try {
                repository.registrar(
                    cursoId = curso.id,
                    docenteId = docenteId,
                    nombre = nombreLimpio,
                    fotoRuta = foto,
                    rut = actual.rut,
                    pin = actual.pin
                )

                _uiState.update {
                    it.copy(
                        nombre = "",
                        rut = "",
                        pin = "",
                        errorAcceso = null,
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
                        mensaje = "No se pudo guardar. Revisa que el RUT no esté registrado."
                    )
                }
            }
        }
    }
}
