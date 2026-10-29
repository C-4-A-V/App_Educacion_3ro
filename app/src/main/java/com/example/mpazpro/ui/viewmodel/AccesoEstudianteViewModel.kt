package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.EstudianteRepository
import com.example.mpazpro.model.AccesoEstudianteUiState
import com.example.mpazpro.model.ProgresoEstudiante
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccesoEstudianteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)

    private val repository = EstudianteRepository(
        estudianteDao = database.estudianteDao(),
        cursoDao = database.cursoDao(),
        actividadDao = database.actividadDao()
    )

    private val _uiState = MutableStateFlow(AccesoEstudianteUiState())
    val uiState = _uiState.asStateFlow()

    private var progresoJob: Job? = null

    init {
        cargarPerfiles()
    }

    private fun cargarPerfiles() {
        viewModelScope.launch {
            try {
                repository.observarPerfiles().collect { perfiles ->
                    _uiState.update { actual ->
                        actual.copy(
                            perfiles = perfiles,
                            cargando = false,
                            // Actualiza el perfil elegido si cambian sus datos.
                            seleccionado = actual.seleccionado?.let { elegido ->
                                perfiles.find { it.id == elegido.id }
                            },
                            mensaje = null
                        )
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensaje = "No se pudieron cargar los perfiles."
                    )
                }
            }
        }
    }

    fun seleccionar(estudianteId: Long) {
        val perfil = _uiState.value.perfiles.find {
            it.id == estudianteId
        } ?: return

        _uiState.update { it.copy(seleccionado = perfil) }

        progresoJob?.cancel()
        progresoJob = viewModelScope.launch {
            try {
                repository.observarProgreso(estudianteId).collect { progreso: ProgresoEstudiante ->
                    _uiState.update { it.copy(progreso = progreso) }
                }
            } catch (_: Exception) {}
        }
    }

    fun salir() {
        progresoJob?.cancel()
        progresoJob = null
        _uiState.update { it.copy(seleccionado = null, progreso = ProgresoEstudiante()) }
    }
}
