package com.example.mpazpro.ui.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.CursoRepository
import com.example.mpazpro.model.CursosUiState
import com.example.mpazpro.model.ConexionCurso
import com.example.mpazpro.model.resumenActualizacionCursos
import com.example.mpazpro.data.repository.VinculacionAutomaticaCursos
import com.example.mpazpro.data.repository.ResultadoConexionCurso
import com.example.mpazpro.data.repository.ResultadoImportacionCursos
import com.example.mpazpro.data.remote.ServicioActividadesServidor
import com.example.mpazpro.data.remote.ServicioAsegurarCurso
import com.example.mpazpro.data.repository.PropuestaVinculacionCursos
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CursosViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)
    private val repository = CursoRepository(database.cursoDao())
    private val sincronizador = VinculacionAutomaticaCursos(database, ServicioActividadesServidor.api, ServicioAsegurarCurso.api)
    private val esDepuracion = (application.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private val _uiState = MutableStateFlow(CursosUiState())
    val uiState = _uiState.asStateFlow()

    private var docenteActualId: Long? = null
    private var observacionJob: Job? = null
    private var conexionJob: Job? = null
    private var conexionSolicitada = false

    fun cerrarAvisoActualizacion() {
        _uiState.update { it.copy(avisoActualizacion = null) }
    }

    fun actualizarDesdePanel() {
        if (!esDepuracion || com.example.mpazpro.data.repository.SesionDocenteRemota.repository.sesion.value != null) {
            _uiState.update { it.copy(avisoActualizacion = "La sincronización de cursos, estudiantes y resultados con esta cuenta aún no está disponible.") }
            return
        }
        if (docenteActualId == null || _uiState.value.cargando || _uiState.value.actualizandoManual) return
        _uiState.update { it.copy(actualizandoManual = true, avisoActualizacion = null) }
        revisarConexiones()
    }

    private fun finalizarActualizacionManual(sinConexion: Boolean) {
        _uiState.update { estado ->
            if (!estado.actualizandoManual) return@update estado
            estado.copy(actualizandoManual = false,
                avisoActualizacion = resumenActualizacionCursos(estado, sinConexion))
        }
    }

    fun actualizarAutomaticamente() {
        if (conexionJob?.isActive != true) revisarConexiones()
    }

    fun revisarConexiones() {
        // El servicio de prueba vincula por nombre y no conoce la cuenta autenticada.
        if (!esDepuracion || com.example.mpazpro.data.repository.SesionDocenteRemota.repository.sesion.value != null) return
        val docenteId = docenteActualId ?: return
        if (conexionJob?.isActive == true) {
            conexionSolicitada = true
            return
        }
        conexionJob = viewModelScope.launch {
            try {
            do {
                conexionSolicitada = false
                var pendientes = false
                val importacion = try {
                    sincronizador.incorporarCompartidos(docenteId)
                } catch (cancelacion: CancellationException) {
                    throw cancelacion
                } catch (_: Exception) {
                    pendientes = true
                    null
                }
                if (docenteActualId != docenteId) return@launch
                val cursos = database.cursoDao().listarPorDocente(docenteId)
                if (importacion != ResultadoImportacionCursos.ACTUALIZADO) {
                    val estado = if (importacion == null) ConexionCurso.SIN_CONEXION else ConexionCurso.REVISAR
                    _uiState.update {
                        it.copy(conexiones = cursos.associate { curso -> curso.id to estado },
                            requiereRevisionGlobal = importacion == ResultadoImportacionCursos.REVISION)
                    }
                } else {
                    _uiState.update { it.copy(requiereRevisionGlobal = false) }
                    for (curso in cursos) {
                        if (docenteActualId != docenteId) return@launch
                        if (pendientes) {
                            _uiState.update { it.copy(conexiones = it.conexiones + (curso.id to ConexionCurso.SIN_CONEXION)) }
                            continue
                        }
                        _uiState.update { it.copy(conexiones = it.conexiones + (curso.id to ConexionCurso.CONECTANDO)) }
                        val resultado = try {
                            when (sincronizador.conectar(docenteId, curso.id)) {
                                ResultadoConexionCurso.CONECTADO -> ConexionCurso.CONECTADO
                                ResultadoConexionCurso.REVISION -> ConexionCurso.REVISAR
                            }
                        } catch (cancelacion: CancellationException) {
                            throw cancelacion
                        } catch (_: Exception) {
                            pendientes = true
                            ConexionCurso.SIN_CONEXION
                        }
                        if (docenteActualId == docenteId) _uiState.update {
                            it.copy(conexiones = it.conexiones + (curso.id to resultado))
                        }
                    }
                }
                if (!conexionSolicitada && docenteActualId == docenteId) finalizarActualizacionManual(pendientes)
            } while (conexionSolicitada && docenteActualId == docenteId)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (docenteActualId == docenteId) _uiState.update {
                    it.copy(actualizandoManual = false, avisoActualizacion = "No se pudieron revisar los cursos. Inténtalo nuevamente. Los estudiantes y resultados todavía no se sincronizan entre dispositivos.")
                }
            }
        }
    }

    fun cargarCursos(docenteId: Long) {
        // Evita iniciar varias observaciones para el mismo docente.
        if (docenteActualId == docenteId) return

        observacionJob?.cancel()
        conexionJob?.cancel()
        conexionSolicitada = false
        docenteActualId = docenteId
        _uiState.value = CursosUiState()

        observacionJob = viewModelScope.launch {
            try {
                repository.observarCursos(docenteId).collect { cursos ->
                    _uiState.update {
                        it.copy(cursos = cursos, cargando = false,
                            conexiones = it.conexiones.filterKeys { id -> cursos.any { curso -> curso.id == id } })
                    }
                    revisarConexiones()
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensaje = "No se pudieron cargar los cursos."
                    )
                }
            }
        }
    }

    fun crearCurso(seccionConfirmada: Char) {
        val docenteId = docenteActualId ?: return
        val actual = _uiState.value

        if (actual.guardando || actual.cargando) return

        if (PropuestaVinculacionCursos.siguiente(actual.cursos.map { it.nombre }) != seccionConfirmada) {
            _uiState.update { it.copy(mensaje = "La lista de cursos cambió o necesita revisión. Vuelve a intentarlo.") }
            return
        }

        _uiState.update {
            it.copy(
                guardando = true,
                errorNombre = null,
                mensaje = null
            )
        }

        viewModelScope.launch {
            try {
                repository.crearSiguiente(docenteId, seccionConfirmada)
                _uiState.update { it.copy(guardando = false, mensaje = "Se creó 3.º Básico $seccionConfirmada en este dispositivo; todavía no se sincroniza.") }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje = "No se pudo crear el curso. Revisa la lista y vuelve a intentarlo."
                    )
                }
            }
        }
    }
}
