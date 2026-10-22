package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.ActividadRepository
import com.example.mpazpro.data.repository.CursoRepository
import com.example.mpazpro.model.ActividadesDocenteUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActividadesDocenteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)

    private val cursos = CursoRepository(database.cursoDao())
    private val actividades = ActividadRepository(database)

    private val _uiState = MutableStateFlow(
        ActividadesDocenteUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var contexto: Triple<Long, String, Long>? = null
    private var observacionJob: Job? = null

    fun cargar(
        cursoId: Long,
        asignaturaId: String,
        docenteId: Long
    ) {
        val nuevoContexto = Triple(
            cursoId,
            asignaturaId,
            docenteId
        )

        if (contexto == nuevoContexto) return

        observacionJob?.cancel()
        contexto = nuevoContexto
        _uiState.value = ActividadesDocenteUiState()

        observacionJob = viewModelScope.launch {
            try {
                val curso = cursos.obtenerCurso(
                    cursoId = cursoId,
                    docenteId = docenteId
                )

                if (curso == null) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensaje = "Curso no disponible."
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(curso = curso)
                }

                actividades.observarActividades(
                    cursoId = cursoId,
                    asignaturaId = asignaturaId,
                    docenteId = docenteId
                ).collect { lista ->
                    _uiState.update {
                        it.copy(
                            actividades = lista,
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
                        mensaje = "No se pudieron cargar las actividades."
                    )
                }
            }
        }
    }
}