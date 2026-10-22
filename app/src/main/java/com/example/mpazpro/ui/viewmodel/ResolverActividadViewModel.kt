package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.ActividadEstudianteRepository
import com.example.mpazpro.model.ResolverActividadUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResolverActividadViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ActividadEstudianteRepository(
        MpazDatabase.obtenerInstancia(application)
    )

    private val _uiState = MutableStateFlow(
        ResolverActividadUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var contexto: Pair<Long, Long>? = null
    private var cargaJob: Job? = null

    fun cargar(
        estudianteId: Long,
        actividadId: Long
    ) {
        val nuevoContexto = estudianteId to actividadId

        if (contexto == nuevoContexto) return

        cargaJob?.cancel()
        contexto = nuevoContexto
        _uiState.value = ResolverActividadUiState()

        cargaJob = viewModelScope.launch {
            try {
                val detalle = repository.obtenerActividad(
                    estudianteId = estudianteId,
                    actividadId = actividadId
                )

                if (detalle == null) {
                    _uiState.value = ResolverActividadUiState(
                        cargando = false,
                        mensaje = "Esta actividad no está disponible para tu curso."
                    )
                    return@launch
                }

                val (actividad, intento) = detalle

                _uiState.value = ResolverActividadUiState(
                    actividad = actividad,
                    respuestaSeleccionada = intento?.respuestaElegida ?: -1,
                    intento = intento,
                    cargando = false
                )
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.value = ResolverActividadUiState(
                    cargando = false,
                    mensaje = "No se pudo cargar la actividad."
                )
            }
        }
    }

    fun seleccionarRespuesta(indice: Int) {
        val actual = _uiState.value

        if (
            indice !in 0..3 ||
            actual.cargando ||
            actual.enviando ||
            actual.intento != null
        ) return

        _uiState.update {
            it.copy(
                respuestaSeleccionada = indice,
                mensaje = null
            )
        }
    }

    fun enviarRespuesta() {
        val actual = _uiState.value
        val actividad = actual.actividad ?: return
        val estudianteId = contexto?.first ?: return

        if (
            actual.cargando ||
            actual.enviando ||
            actual.intento != null
        ) return

        if (actual.respuestaSeleccionada !in 0..3) {
            _uiState.update {
                it.copy(
                    mensaje = "Selecciona una alternativa antes de enviar."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                enviando = true,
                mensaje = null
            )
        }

        viewModelScope.launch {
            try {
                val intento = repository.enviarRespuesta(
                    estudianteId = estudianteId,
                    actividadId = actividad.id,
                    respuestaElegida = actual.respuestaSeleccionada
                )

                _uiState.update {
                    it.copy(
                        intento = intento,
                        respuestaSeleccionada = intento.respuestaElegida,
                        enviando = false
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        enviando = false,
                        mensaje = "No se pudo guardar tu respuesta. Inténtalo nuevamente."
                    )
                }
            }
        }
    }
}
