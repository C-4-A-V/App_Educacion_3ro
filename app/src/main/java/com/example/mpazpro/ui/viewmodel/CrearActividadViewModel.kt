package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.ActividadRepository
import com.example.mpazpro.data.repository.CursoRepository
import com.example.mpazpro.model.ActividadFormulario
import com.example.mpazpro.model.CrearActividadUiState
import com.example.mpazpro.validation.ActividadValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CrearActividadViewModel(
    application: Application,
    private val savedState: androidx.lifecycle.SavedStateHandle
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)

    private val cursos = CursoRepository(database.cursoDao())
    private val actividades = ActividadRepository(database)

    private val _uiState = MutableStateFlow(
        CrearActividadUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var contexto: Pair<Long, Long>? = null
    private var asignaturaActual: String? = null
    private var cargaJob: Job? = null

    fun cargar(
        cursoId: Long,
        docenteId: Long,
        asignaturaId: String
    ) {
        val nuevoContexto = cursoId to docenteId
        if (contexto == nuevoContexto && asignaturaActual == asignaturaId) return

        cargaJob?.cancel()
        contexto = nuevoContexto
        asignaturaActual = asignaturaId
        val prefijo = "actividad_${cursoId}_${docenteId}_${asignaturaId}_"
        _uiState.value = CrearActividadUiState(formulario = ActividadFormulario(
            titulo = savedState[prefijo + "titulo"] ?: "",
            instrucciones = savedState[prefijo + "instrucciones"] ?: "",
            pregunta = savedState[prefijo + "pregunta"] ?: "",
            explicacion = savedState[prefijo + "explicacion"] ?: "",
            alternativas = savedState.get<ArrayList<String>>(prefijo + "alternativas")?.toList() ?: List(4) { "" },
            respuestaCorrecta = savedState[prefijo + "respuesta"] ?: -1,
            maxIntentos = savedState[prefijo + "intentos"] ?: 3
        ))

        cargaJob = viewModelScope.launch {
            try {
                val curso = cursos.obtenerCurso(
                    cursoId = cursoId,
                    docenteId = docenteId
                )

                _uiState.update {
                    it.copy(
                        curso = curso,
                        cargando = false,
                        mensaje = if (curso == null) {
                            "Curso no disponible."
                        } else {
                            null
                        }
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensaje = "No se pudo cargar el curso."
                    )
                }
            }
        }
    }

    private fun editar(
        cambio: (ActividadFormulario) -> ActividadFormulario
    ) {
        val actual = _uiState.value

        if (actual.guardando || actual.guardado) return

        _uiState.update {
            it.copy(
                formulario = cambio(it.formulario),
                errores = if (it.intentoValidar) ActividadValidator.errores(cambio(it.formulario)) else it.errores,
                mensaje = null
            )
        }
        guardarBorrador()
    }

    private fun guardarBorrador() {
        val (curso, docente) = contexto ?: return
        val prefijo = "actividad_${curso}_${docente}_${asignaturaActual}_"
        val f = _uiState.value.formulario
        savedState[prefijo + "titulo"] = f.titulo
        savedState[prefijo + "instrucciones"] = f.instrucciones
        savedState[prefijo + "pregunta"] = f.pregunta
        savedState[prefijo + "explicacion"] = f.explicacion
        savedState[prefijo + "alternativas"] = ArrayList(f.alternativas)
        savedState[prefijo + "respuesta"] = f.respuestaCorrecta
        savedState[prefijo + "intentos"] = f.maxIntentos
    }

    fun actualizarExplicacion(valor: String) {
        editar { it.copy(explicacion = valor) }
    }

    fun actualizarMaxIntentos(valor: Int) {
        if (valor in listOf(1, 3)) editar { it.copy(maxIntentos = valor) }
    }

    fun actualizarTitulo(valor: String) {
        editar { it.copy(titulo = valor) }
    }

    fun actualizarInstrucciones(valor: String) {
        editar { it.copy(instrucciones = valor) }
    }

    fun actualizarPregunta(valor: String) {
        editar { it.copy(pregunta = valor) }
    }

    fun actualizarAlternativa(
        indice: Int,
        valor: String
    ) {
        if (indice !in 0..3) return

        editar { formulario ->
            formulario.copy(
                alternativas = formulario.alternativas.mapIndexed {
                        posicion, texto ->
                    if (posicion == indice) valor else texto
                }
            )
        }
    }

    fun seleccionarRespuesta(indice: Int) {
        if (indice !in 0..3) return

        editar {
            it.copy(respuestaCorrecta = indice)
        }
    }

    fun guardar(asignaturaId: String) {
        val actual = _uiState.value
        val curso = actual.curso ?: return
        val docenteId = contexto?.second ?: return

        if (
            actual.cargando ||
            actual.guardando ||
            actual.guardado
        ) return

        val errores = ActividadValidator.errores(actual.formulario)
        _uiState.update { it.copy(errores = errores, intentoValidar = true) }
        if (errores.hayErrores) {
            _uiState.update { it.copy(mensaje = "Revisa los campos marcados antes de guardar.") }
            return
        }

        _uiState.update {
            it.copy(
                guardando = true,
                mensaje = null
            )
        }

        viewModelScope.launch {
            try {
                actividades.crearActividad(
                    cursoId = curso.id,
                    asignaturaId = asignaturaId,
                    docenteId = docenteId,
                    formulario = actual.formulario
                )

                val prefijo = "actividad_${curso.id}_${docenteId}_${asignaturaId}_"
                savedState.keys().filter { it.startsWith(prefijo) }.forEach { savedState.remove<Any>(it) }
                _uiState.update {
                    it.copy(
                        guardando = false,
                        guardado = true
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje = "No se pudo guardar la actividad."
                    )
                }
            }
        }
    }
}