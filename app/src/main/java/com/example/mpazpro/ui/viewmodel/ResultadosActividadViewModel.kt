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

    fun generarReporteJson(): String {
        val estado = _uiState.value
        val actividad = estado.actividad ?: return "{}"
        val resultados = estado.resultados

        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"actividadId\": ${actividad.id},\n")
        sb.append("  \"titulo\": \"${actividad.titulo}\",\n")
        sb.append("  \"asignatura\": \"${actividad.asignaturaId}\",\n")
        sb.append("  \"totalEstudiantes\": ${resultados.size},\n")
        sb.append("  \"totalRespondidas\": ${estado.totalRespondidas},\n")
        sb.append("  \"porcentajeAcierto\": \"${estado.porcentajeAciertoTexto}\",\n")
        sb.append("  \"estudiantes\": [\n")
        resultados.forEachIndexed { index, r ->
            sb.append("    {\n")
            sb.append("      \"nombre\": \"${r.estudianteNombre}\",\n")
            sb.append("      \"respondida\": ${r.respondida},\n")
            sb.append("      \"correcta\": ${r.correcta ?: false},\n")
            sb.append("      \"alternativaElegida\": ${r.respuestaElegida ?: -1}\n")
            sb.append("    }${if (index < resultados.size - 1) "," else ""}\n")
        }
        sb.append("  ]\n")
        sb.append("}")
        return sb.toString()
    }
}
