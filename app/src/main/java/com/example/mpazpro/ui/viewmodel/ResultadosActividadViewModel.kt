package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.model.ResultadosActividadUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResultadosActividadViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)
    private val actividadDao = database.actividadDao()
    private val intentoDao = database.intentoActividadDao()

    private val _uiState = MutableStateFlow(ResultadosActividadUiState())
    val uiState = _uiState.asStateFlow()

    private var actividadIdActual: Long? = null
    private var observacionJob: Job? = null

    fun cargar(actividadId: Long) {
        if (actividadIdActual == actividadId) return

        observacionJob?.cancel()
        actividadIdActual = actividadId
        _uiState.value = ResultadosActividadUiState()

        observacionJob = viewModelScope.launch {
            try {
                val actividad = actividadDao.buscarPorId(actividadId)

                if (actividad == null) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensaje = "Actividad no encontrada."
                        )
                    }
                    return@launch
                }

                _uiState.update { it.copy(actividad = actividad) }

                intentoDao.observarResultadosActividad(actividadId).collect { resultados ->
                    _uiState.update {
                        it.copy(
                            resultados = resultados,
                            cargando = false,
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
                        mensaje = "No se pudieron cargar los resultados."
                    )
                }
            }
        }
    }
}
