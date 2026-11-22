package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.CursoRepository
import com.example.mpazpro.data.repository.ActividadRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import com.example.mpazpro.model.CursoDocenteUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CursoDocenteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)
    private val repository = CursoRepository(database.cursoDao())

    private val _uiState = MutableStateFlow(
        CursoDocenteUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var contextoActual: Pair<Long, Long>? = null
    private var cargaJob: Job? = null
    private var vinculoJob: Job? = null

    fun prepararBanco() {
        val actual = contextoActual ?: return
        if (_uiState.value.preparandoBanco || !_uiState.value.vinculado || _uiState.value.curso == null) return
        _uiState.update { it.copy(preparandoBanco = true, avisoBanco = null) }
        viewModelScope.launch {
            try {
                val total = ActividadRepository(database).prepararBancoParaPublicacion(actual.first, actual.second)
                if (contextoActual == actual) _uiState.update {
                    it.copy(avisoBanco = if (total == 0) "No hay preguntas nuevas del banco para preparar."
                        else "Se prepararon $total preguntas. Elige cuáles compartir en cada asignatura.")
                }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                if (contextoActual == actual) _uiState.update { it.copy(avisoBanco = "No se pudo preparar el banco de preguntas.") }
            } finally {
                if (contextoActual == actual) _uiState.update { it.copy(preparandoBanco = false) }
            }
        }
    }

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
        vinculoJob?.cancel()
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
                    mensaje = if (curso == null) "Este curso no está disponible para tu cuenta." else null
                )
                if (curso != null) {
                    vinculoJob = viewModelScope.launch {
                        database.vinculoCursoDao().observar(cursoId).collect { vinculo ->
                            if (contextoActual == nuevoContexto) _uiState.update { it.copy(vinculado = vinculo != null) }
                        }
                    }
                }
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