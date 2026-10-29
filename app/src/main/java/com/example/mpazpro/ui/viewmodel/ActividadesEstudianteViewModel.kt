package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.ActividadEstudianteRepository
import com.example.mpazpro.model.ActividadesEstudianteUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActividadesEstudianteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ActividadEstudianteRepository(
        MpazDatabase.obtenerInstancia(application)
    )

    private val _uiState = MutableStateFlow(
        ActividadesEstudianteUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var contexto: Pair<Long, String>? = null
    private var observacionJob: Job? = null

    fun cargar(
        estudianteId: Long,
        asignaturaId: String
    ) {
        val nuevoContexto = estudianteId to asignaturaId

        if (contexto == nuevoContexto) return

        observacionJob?.cancel()
        contexto = nuevoContexto
        _uiState.value = ActividadesEstudianteUiState()

        observacionJob = viewModelScope.launch {
            try {
                launch {
                    repository.observarActividades(
                        estudianteId = estudianteId,
                        asignaturaId = asignaturaId
                    ).collect { lista ->
                        _uiState.update {
                            it.copy(
                                actividades = lista,
                                cargando = false,
                                mensaje = null
                            )
                        }
                    }
                }

                launch {
                    repository.observarProgresoAsignatura(
                        estudianteId = estudianteId,
                        asignaturaId = asignaturaId
                    ).collect { progreso ->
                        _uiState.update {
                            it.copy(progreso = progreso)
                        }
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensaje = "No se pudieron cargar las actividades."
                    )
                }
            }
        }
    }
}
