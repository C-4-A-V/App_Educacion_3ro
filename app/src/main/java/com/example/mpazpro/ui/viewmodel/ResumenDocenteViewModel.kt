package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ResumenDocenteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = MpazDatabase.obtenerInstancia(application)

    private val _uiState = MutableStateFlow(
        ResumenDocenteUiState()
    )
    val uiState = _uiState.asStateFlow()

    private var consulta: Job? = null
    private var docenteActual: Long? = null

    fun cargar(docenteId: Long) {
        if (
            docenteActual == docenteId &&
            consulta?.isActive == true
        ) {
            return
        }

        consulta?.cancel()
        docenteActual = docenteId
        _uiState.value = ResumenDocenteUiState()

        consulta = viewModelScope.launch {
            try {
                combine(
                    db.cursoDao().observarPorDocente(docenteId),
                    db.estudianteDao().observarAvancesDocente(docenteId)
                ) { cursos, filas ->

                    val alumnos = filas
                        .groupBy { it.estudianteId }
                        .values
                        .map { registros ->

                            val alumno = registros.first()

                            ResumenAlumno(
                                id = alumno.estudianteId,
                                nombre = alumno.nombre,
                                fotoRuta = alumno.fotoRuta,
                                cursoId = alumno.cursoId,
                                materias = Asignatura.entries.associate { materia ->

                                    val fila = registros.find {
                                        it.asignaturaId == materia.id
                                    }

                                    materia.id to ProgresoEstudiante(
                                        total = fila?.total ?: 0,
                                        completadas = fila?.completadas ?: 0,
                                        correctas = fila?.correctas ?: 0
                                    )
                                }
                            )
                        }

                    ResumenDocenteUiState(
                        cursos = cursos,
                        alumnos = alumnos,
                        cargando = false
                    )
                }.collect {
                    _uiState.value = it
                }

            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.value = ResumenDocenteUiState(
                    cargando = false,
                    error = "No se pudieron cargar los resultados. " +
                        "Vuelve a intentarlo."
                )
            }
        }
    }
}
