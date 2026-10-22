package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.CursoRepository
import com.example.mpazpro.model.CursoDocenteUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CursoDocenteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = CursoRepository(
        MpazDatabase.obtenerInstancia(application).cursoDao()
    )

    private val _uiState = MutableStateFlow(
        CursoDocenteUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var contextoActual: Pair<Long, Long>? = null
    private var cargaJob: Job? = null

    fun cargar(
        cursoId: Long,
        docenteId: Long
    ) {
        val nuevoContexto = cursoId to docenteId

        if (
            contextoActual == nuevoContexto &&
            _uiState.value.curso != null
        ) return

        cargaJob?.cancel()
        contextoActual = nuevoContexto
        _uiState.value = CursoDocenteUiState()

        cargaJob = viewModelScope.launch {
            try {
                val curso = repository.obtenerCurso(
                    cursoId = cursoId,
                    docenteId = docenteId
                )

                _uiState.value = CursoDocenteUiState(
                    curso = curso,
                    cargando = false,
                    mensaje = if (curso == null) {
                        "Este curso no está disponible para tu cuenta."
                    } else {
                        null
                    }
                )
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.value = CursoDocenteUiState(
                    cargando = false,
                    mensaje = "No se pudo cargar el curso."
                )
            }
        }
    }
}