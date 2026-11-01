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
        docenteId: Long,
        forzar: Boolean = false
    ) {
        val nuevoContexto = actividadId to docenteId

        if (contextoActual == nuevoContexto && !forzar && observacionJob?.isActive == true) return

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
                    it.copy(actividad = actividad, pistaBorrador = actividad.pista)
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

    fun recargar() {
        if (_uiState.value.guardandoPista) return
        contextoActual?.let { cargar(it.first, it.second, forzar = true) }
    }

    fun actualizarPista(valor: String) {
        if (_uiState.value.guardandoPista) return
        _uiState.update { it.copy(pistaBorrador = valor, mensajePista = null) }
    }

    fun guardarPista() {
        val (actividadId, docenteId) = contextoActual ?: return
        val actual = _uiState.value
        if (actual.guardandoPista || actual.actividad == null) return
        val pista = actual.pistaBorrador.trim()
        if (pista.length > 500) {
            _uiState.update { it.copy(mensajePista = "Usa hasta 500 caracteres.") }
            return
        }
        _uiState.update { it.copy(guardandoPista = true, mensajePista = null) }
        viewModelScope.launch {
            try {
                check(actividadDao.actualizarPista(actividadId, docenteId, pista) == 1)
                if (contextoActual == actividadId to docenteId) {
                    _uiState.update { it.copy(
                        actividad = it.actividad?.copy(pista = pista),
                        pistaBorrador = pista, guardandoPista = false,
                        mensajePista = "Pista guardada."
                    ) }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (contextoActual == actividadId to docenteId) {
                    _uiState.update { it.copy(guardandoPista = false, mensajePista = "No se pudo guardar la pista.") }
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

                    put("intentosUsados", resultado.intentosUsados)
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
            put("tipo", if (actividad.maxIntentos == 3) "practica" else "evaluacion")
            put("titulo", actividad.titulo)
            put("asignatura", actividad.asignaturaId)
            put("totalEstudiantes", estado.totalEstudiantes)
            put("totalRespondidas", estado.totalRespondidas)
            put("porcentajeAcierto", estado.porcentajeAciertoTexto)
            put("estudiantes", filas)
        }.toString(2)
    }
}
