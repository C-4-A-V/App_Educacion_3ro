package com.example.mpazpro.ui.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.ActividadEstudianteRepository
import com.example.mpazpro.data.repository.AulaEstudianteRepository
import com.example.mpazpro.data.repository.PreferenciasAulaEstudiante
import com.example.mpazpro.data.remote.ServicioActividadesServidor
import com.example.mpazpro.data.remote.ServicioAulaActividades
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

    private val database = MpazDatabase.obtenerInstancia(application)
    private val repository = ActividadEstudianteRepository(database)
    private val aula = AulaEstudianteRepository(
        database, ServicioActividadesServidor.api, ServicioAulaActividades.api,
        PreferenciasAulaEstudiante(application)
    )
    private val esDepuracion = (application.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private val _uiState = MutableStateFlow(
        ActividadesEstudianteUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var contexto: Pair<Long, String>? = null
    private var observacionJob: Job? = null
    private var consultaAulaJob: Job? = null

    fun cargar(
        estudianteId: Long,
        asignaturaId: String
    ) {
        val nuevoContexto = estudianteId to asignaturaId

        if (contexto == nuevoContexto) return

        observacionJob?.cancel()
        consultaAulaJob?.cancel()
        contexto = nuevoContexto
        _uiState.value = ActividadesEstudianteUiState()
        if (esDepuracion) actualizarCompartidas()

        observacionJob = viewModelScope.launch {
            try {
                // Las actividades locales se conservan, pero solo se ofrecen las compartidas.
                _uiState.update { it.copy(actividades = emptyList(), cargando = false, mensaje = null) }

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

    fun actualizarCompartidasAutomaticamente() {
        if (consultaAulaJob?.isActive != true) actualizarCompartidas()
    }

    fun actualizarCompartidas() {
        if (!esDepuracion) return
        val consulta = contexto ?: return
        consultaAulaJob?.cancel()
        _uiState.update { it.copy(cargandoCompartidas = true, errorCompartidas = false) }
        consultaAulaJob = viewModelScope.launch {
            try {
                val lista = aula.listar(consulta.first, consulta.second).orEmpty()
                if (contexto == consulta) _uiState.update {
                    it.copy(actividadesCompartidas = lista, cargandoCompartidas = false)
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (contexto == consulta) _uiState.update {
                    it.copy(cargandoCompartidas = false, errorCompartidas = true)
                }
            }
        }
    }
}
