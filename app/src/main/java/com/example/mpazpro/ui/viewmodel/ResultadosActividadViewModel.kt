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
import org.json.JSONArray
import org.json.JSONObject

class ResultadosActividadViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)

    private val actividadDao = database.actividadDao()
    private val intentoDao = database.intentoActividadDao()

    private val _uiState = MutableStateFlow(
        ResultadosActividadUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var contextoActual: Pair<Long, Long>? = null
    private var observacionJob: Job? = null

    fun cargar(
        actividadId: Long,
        docenteId: Long
    ) {
        val nuevoContexto = actividadId to docenteId

        if (contextoActual == nuevoContexto) return

        observacionJob?.cancel()
        contextoActual = nuevoContexto
        _uiState.value = ResultadosActividadUiState()

        observacionJob = viewModelScope.launch {
            try {
                val actividad = actividadDao.buscarPorIdYDocente(
                    actividadId = actividadId,
                    docenteId = docenteId
                )

                if (actividad == null) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensaje = "Actividad no encontrada."
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(actividad = actividad)
                }

                intentoDao.observarResultadosActividad(
                    actividadId = actividadId,
                    docenteId = docenteId
                ).collect { resultados ->
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

        val filas = JSONArray()

        estado.resultados.forEach { resultado ->
            filas.put(
                JSONObject().apply {
                    put(
                        "nombre",
                        resultado.estudianteNombre
                    )

                    put(
                        "respondida",
                        resultado.respondida
                    )

                    put(
                        "correcta",
                        resultado.correcta ?: JSONObject.NULL
                    )

                    put(
                        "alternativaElegida",
                        resultado.respuestaElegida ?: JSONObject.NULL
                    )
                }
            )
        }

        return JSONObject().apply {
            put("actividadId", actividad.id)
            put("titulo", actividad.titulo)
            put("asignatura", actividad.asignaturaId)
            put("totalEstudiantes", estado.totalEstudiantes)
            put("totalRespondidas", estado.totalRespondidas)
            put("porcentajeAcierto", estado.porcentajeAciertoTexto)
            put("estudiantes", filas)
        }.toString(2)
    }
}
