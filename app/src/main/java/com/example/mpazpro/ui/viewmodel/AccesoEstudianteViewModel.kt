package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.local.DatosDemostracion
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
    application: Application,
    private val savedState: androidx.lifecycle.SavedStateHandle
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
                DatosDemostracion.preparar(database)
                repository.observarPerfiles().collect { perfiles ->
                    _uiState.update { actual ->
                        actual.copy(
                            perfiles = perfiles,
                            cargando = false,
                            // Actualiza el perfil elegido si cambian sus datos.
                            seleccionado = perfiles.find { it.id == (actual.seleccionado?.id ?: savedState.get<Long>("estudianteId")) },
                            mensaje = null
                        )
                    }
                    if (progresoJob == null) {
                        _uiState.value.seleccionado?.let { seleccionar(it.id) }
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

        savedState["estudianteId"] = estudianteId
        _uiState.update { it.copy(seleccionado = perfil, progreso = ProgresoEstudiante(), mensaje = null) }

        progresoJob?.cancel()
        progresoJob = viewModelScope.launch {
            try {
                repository.observarProgreso(estudianteId).collect { progreso: ProgresoEstudiante ->
                    _uiState.update { it.copy(progreso = progreso) }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(mensaje = "No se pudo actualizar tu progreso. Vuelve a seleccionar tu perfil.") }
            }
        }
    }

    fun salir() {
        savedState.remove<Long>("estudianteId")
        progresoJob?.cancel()
        progresoJob = null
        _uiState.update { it.copy(seleccionado = null, progreso = ProgresoEstudiante()) }
    }
}
