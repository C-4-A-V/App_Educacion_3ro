package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.IntentoActividadEntity
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.remote.ResultadoRespuestaAula
import com.example.mpazpro.data.remote.ServicioActividadesServidor
import com.example.mpazpro.data.remote.ServicioAulaActividades
import com.example.mpazpro.data.repository.AulaEstudianteRepository
import com.example.mpazpro.data.repository.PreferenciasAulaEstudiante
import com.example.mpazpro.data.repository.RespuestaAulaRepository
import com.example.mpazpro.model.ActividadParaResolver
import com.example.mpazpro.model.ResolverActividadUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResolverActividadRemotaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MpazDatabase.obtenerInstancia(application)
    private val preferencias = PreferenciasAulaEstudiante(application)
    private val aula = AulaEstudianteRepository(database, ServicioActividadesServidor.api, ServicioAulaActividades.api, preferencias)
    private val respuestas = RespuestaAulaRepository(aula, preferencias, ServicioAulaActividades.api)
    private val _uiState = MutableStateFlow(ResolverActividadUiState())
    val uiState = _uiState.asStateFlow()
    private var contexto: Pair<Long, Long>? = null
    private var carga: Job? = null
    private var continuacion: Job? = null
    private var respuestaPendiente = false

    fun cargar(estudianteId: Long, actividadId: Long, forzar: Boolean = false) {
        val nuevo = estudianteId to actividadId
        if (contexto == nuevo && !forzar) return
        carga?.cancel()
        continuacion?.cancel()
        contexto = nuevo
        respuestaPendiente = false
        _uiState.value = ResolverActividadUiState()
        carga = viewModelScope.launch {
            try {
                val remota = requireNotNull(aula.obtener(estudianteId, actividadId))
                check(remota.maxIntentos == 1 || remota.maxIntentos == 3)
                val actividad = ActividadParaResolver(
                    remota.id, remota.asignaturaId, remota.titulo, remota.instrucciones, remota.pregunta,
                    remota.alternativaA, remota.alternativaB, remota.alternativaC, remota.alternativaD,
                    remota.maxIntentos, remota.pista
                )
                val resultado = respuestas.recuperar(estudianteId, actividadId)
                if (contexto == nuevo) {
                    _uiState.value = ResolverActividadUiState(
                        actividad = actividad,
                        intento = resultado?.aIntento(estudianteId, actividadId),
                        respuestaSeleccionada = resultado?.respuestaElegida ?: -1,
                        explicacion = resultado?.explicacion.orEmpty(),
                        cargando = false,
                        cargandoContinuacion = false
                    )
                    if (resultado?.finalizada == true) actualizarContinuacion()
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (contexto == nuevo) _uiState.value = ResolverActividadUiState(
                    cargando = false,
                    cargandoContinuacion = false,
                    mensaje = "No pudimos cargar esta actividad. Comprueba la conexión e inténtalo otra vez."
                )
            }
        }
    }

    fun recargar() {
        if (_uiState.value.enviando) return
        contexto?.let { cargar(it.first, it.second, forzar = true) }
    }

    fun seleccionarRespuesta(indice: Int) {
        val actual = _uiState.value
        if (indice !in 0..3 || actual.cargando || actual.enviando || respuestaPendiente ||
            (actual.intento != null && !actual.reintentando)) return
        _uiState.update { it.copy(respuestaSeleccionada = indice, mensaje = null) }
    }

    fun reintentar() {
        val actual = _uiState.value
        if (actual.enviando || actual.intento?.finalizada != false) return
        _uiState.update { it.copy(reintentando = true, respuestaSeleccionada = -1, mensaje = null) }
    }

    fun enviarRespuesta() {
        val actual = _uiState.value
        val (estudianteId, actividadId) = contexto ?: return
        if (actual.cargando || actual.enviando || actual.actividad == null ||
            (actual.intento != null && !actual.reintentando)) return
        if (actual.respuestaSeleccionada !in 0..3) {
            _uiState.update { it.copy(mensaje = "Selecciona una alternativa antes de enviar.") }
            return
        }
        val numero = (actual.intento?.numero ?: 0) + 1
        val seleccion = actual.respuestaSeleccionada
        _uiState.update { it.copy(enviando = true, mensaje = null) }
        viewModelScope.launch {
            try {
                val resultado = respuestas.responder(estudianteId, actividadId, numero, seleccion)
                respuestaPendiente = false
                _uiState.update {
                    it.copy(
                        intento = resultado.aIntento(estudianteId, actividadId),
                        respuestaSeleccionada = resultado.respuestaElegida,
                        seleccionBloqueada = false,
                        explicacion = resultado.explicacion.orEmpty(),
                        enviando = false,
                        reintentando = false,
                        cargandoContinuacion = false
                    )
                }
                if (resultado.finalizada) actualizarContinuacion()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                respuestaPendiente = true
                _uiState.update {
                    it.copy(enviando = false, seleccionBloqueada = true, mensaje = "No pudimos confirmar tu respuesta. Intenta enviar la misma otra vez.")
                }
            }
        }
    }

    fun actualizarContinuacion() {
        val actual = contexto ?: return
        val asignatura = _uiState.value.actividad?.asignaturaId ?: return
        if (_uiState.value.intento?.finalizada != true) return
        continuacion?.cancel()
        _uiState.update { it.copy(cargandoContinuacion = true, errorContinuacion = false, siguienteActividadId = null) }
        continuacion = viewModelScope.launch {
            try {
                val siguiente = respuestas.siguiente(actual.first, actual.second, asignatura)
                if (contexto == actual) _uiState.update {
                    it.copy(cargandoContinuacion = false, siguienteActividadId = siguiente)
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (contexto == actual) _uiState.update { it.copy(cargandoContinuacion = false, errorContinuacion = true) }
            }
        }
    }

    private fun ResultadoRespuestaAula.aIntento(estudianteId: Long, actividadId: Long) =
        IntentoActividadEntity(
            estudianteId = estudianteId,
            actividadId = actividadId,
            respuestaElegida = respuestaElegida,
            respuestaCorrecta = respuestaCorrecta ?: -1,
            correcta = correcta,
            numero = numero,
            finalizada = finalizada
        )
}
