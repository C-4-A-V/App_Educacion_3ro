package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.remote.ServicioActividadesServidor
import com.example.mpazpro.data.repository.ConflictoCursoLocal
import com.example.mpazpro.data.repository.CursoRepository
import com.example.mpazpro.data.repository.CursosAutenticadosRepository
import com.example.mpazpro.data.repository.OperacionDocenteRechazada
import com.example.mpazpro.data.repository.PropuestaVinculacionCursos
import com.example.mpazpro.data.repository.SesionDocenteExpirada
import com.example.mpazpro.data.repository.SesionDocenteRemota
import com.example.mpazpro.model.ConexionCurso
import com.example.mpazpro.model.CursosUiState
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CursosViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MpazDatabase.obtenerInstancia(application)
    private val locales = CursoRepository(database.cursoDao())
    private val cursos = CursosAutenticadosRepository(
        database, SesionDocenteRemota.repository, ServicioActividadesServidor.api
    )
    private val _uiState = MutableStateFlow(CursosUiState())
    val uiState = _uiState.asStateFlow()

    private var docenteActualId: Long? = null
    private var observacionJob: Job? = null
    private var conexionJob: Job? = null

    fun cerrarAvisoActualizacion() {
        _uiState.update { it.copy(avisoActualizacion = null) }
    }

    fun cargarCursos(docenteId: Long) {
        if (docenteActualId == docenteId) return
        observacionJob?.cancel()
        conexionJob?.cancel()
        docenteActualId = docenteId
        _uiState.value = CursosUiState()
        observacionJob = viewModelScope.launch {
            try {
                locales.observarCursos(docenteId).collect { lista ->
                    if (docenteActualId != docenteId) return@collect
                    _uiState.update { estado ->
                        estado.copy(cursos = lista, cargando = false,
                            conexiones = lista.associate { curso ->
                                curso.id to if (curso.id in estado.localesVinculados)
                                    ConexionCurso.CONECTADO else ConexionCurso.REVISAR
                            })
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (docenteActualId == docenteId) _uiState.update {
                    it.copy(cargando = false, mensaje = "No se pudieron cargar los cursos guardados.")
                }
            }
        }
        revisarConexiones()
    }

    fun actualizarDesdePanel() = sincronizar(manual = true)
    fun actualizarAutomaticamente() = sincronizar(manual = false)
    fun revisarConexiones() = sincronizar(manual = false)

    private fun sincronizar(manual: Boolean) {
        val docenteId = docenteActualId ?: return
        if (conexionJob?.isActive == true || _uiState.value.guardando) return
        if (manual) _uiState.update { it.copy(actualizandoManual = true, avisoActualizacion = null) }
        conexionJob = viewModelScope.launch {
            try {
                val resultado = cursos.reintentarPendientes(docenteId)
                val vinculados = cursos.localesVinculados(docenteId)
                actualizarPendientesLocales(docenteId)
                if (docenteActualId != docenteId) return@launch
                _uiState.update { estado ->
                    estado.copy(
                        localesVinculados = vinculados,
                        remotosSinVincular = resultado.noVinculados,
                        operacionesPendientes = resultado.pendientes,
                        conexiones = estado.cursos.associate { curso ->
                            curso.id to if (curso.id in vinculados) ConexionCurso.CONECTADO else ConexionCurso.REVISAR
                        },
                        actualizandoManual = false,
                        avisoActualizacion = if (manual)
                            "Cursos actualizados. Los estudiantes, fotos y resultados aún no se sincronizan entre dispositivos."
                            else estado.avisoActualizacion,
                        mensaje = if (resultado.noVinculados.isNotEmpty())
                            "Hay cursos que necesitan asociación. Revisa los cursos guardados."
                            else null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                actualizarPendientesLocales(docenteId)
                if (docenteActualId == docenteId) _uiState.update {
                    it.copy(actualizandoManual = false,
                        conexiones = it.cursos.associate { curso ->
                            curso.id to if (curso.id in it.localesVinculados)
                                ConexionCurso.SIN_CONEXION else ConexionCurso.REVISAR
                        },
                        avisoActualizacion = if (manual) mensajeError(e) else it.avisoActualizacion,
                        mensaje = if (manual) it.mensaje else mensajeError(e))
                }
            }
        }
    }

    fun crearCurso(seccionConfirmada: Char) {
        val id = docenteActualId ?: return
        val estado = _uiState.value
        if (estado.guardando || estado.cargando) return
        if (PropuestaVinculacionCursos.siguiente(estado.cursos.map { it.nombre }) != seccionConfirmada) {
            _uiState.update { it.copy(mensaje = "La lista cambió. Vuelve a revisar el curso.") }
            return
        }
        operar(id, "Curso creado y confirmado por el servidor.") {
            cursos.crear(id, seccionConfirmada)
        }
    }

    fun crearDesdeAnterior(cursoId: Long) {
        val id = docenteActualId ?: return
        val curso = _uiState.value.cursos.firstOrNull { it.id == cursoId } ?: return
        val seccion = PropuestaVinculacionCursos.seccion(curso.nombre)
        if (seccion == null) {
            _uiState.update { it.copy(mensaje = "No se pudo reconocer la sección de este curso. Sus datos se conservan.") }
            return
        }
        operar(id, "Curso conectado. Sus datos guardados se conservan.") {
            cursos.crearDesdeAnterior(id, cursoId, seccion)
        }
    }

    fun conciliarPendiente(cursoLocalId: Long) {
        val id = docenteActualId ?: return
        val local = _uiState.value.cursos.firstOrNull { it.id == cursoLocalId } ?: return
        val seccion = PropuestaVinculacionCursos.seccion(local.nombre) ?: return
        operar(id, "Creación confirmada y curso asociado.") {
            cursos.conciliarPendienteConCursoLocal(id, cursoLocalId, seccion)
        }
    }

    fun asociarExistente(cursoLocalId: Long, cursoServidorId: Long) {
        val id = docenteActualId ?: return
        operar(id, "Curso asociado. Sus datos guardados se conservan.") {
            cursos.asociarExistente(id, cursoLocalId, cursoServidorId)
        }
    }

    private fun operar(docenteId: Long, exito: String, accion: suspend () -> Any) {
        if (_uiState.value.guardando) return
        if (conexionJob?.isActive == true) {
            _uiState.update { it.copy(mensaje = "Se están actualizando los cursos. Inténtalo nuevamente en un momento.") }
            return
        }
        _uiState.update { it.copy(guardando = true, mensaje = null) }
        conexionJob = viewModelScope.launch {
            var confirmada = false
            try {
                accion()
                confirmada = true
                val resultado = cursos.actualizar(docenteId)
                val vinculados = cursos.localesVinculados(docenteId)
                actualizarPendientesLocales(docenteId)
                if (docenteActualId == docenteId) _uiState.update {
                    it.copy(guardando = false, mensaje = exito,
                        localesVinculados = vinculados, remotosSinVincular = resultado.noVinculados,
                        operacionesPendientes = resultado.pendientes,
                        conexiones = it.cursos.associate { c ->
                            c.id to if (c.id in vinculados) ConexionCurso.CONECTADO else ConexionCurso.REVISAR
                        })
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                actualizarPendientesLocales(docenteId)
                if (docenteActualId == docenteId) _uiState.update {
                    it.copy(guardando = false, mensaje = if (confirmada)
                        "$exito No se pudieron actualizar los demás cursos; vuelve a intentarlo."
                        else mensajeError(e))
                }
            }
        }
    }

    private suspend fun actualizarPendientesLocales(docenteId: Long) {
        try {
            val pendientes = cursos.pendientesLocales(docenteId)
            if (docenteActualId == docenteId) _uiState.update {
                it.copy(operacionesPendientes = pendientes.size,
                    seccionesPendientesDeAsociar = pendientes.filter { p -> p.cursoLocalId == null }
                        .mapNotNull { p -> p.seccion.singleOrNull() }.toSet())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Conserva el último estado conocido si Room no puede consultarse.
        }
    }

    private fun mensajeError(error: Exception): String = when (error) {
        is SesionDocenteExpirada -> "La sesión terminó. Vuelve a ingresar."
        is OperacionDocenteRechazada -> when (error.codigo) {
            409 -> "El servidor rechazó la creación: esa sección ya está ocupada o requiere revisión. No se vinculó ningún curso."
            403, 404 -> "No tienes permiso para este curso."
            else -> "El servidor rechazó la operación. Revisa los datos e inténtalo nuevamente."
        }
        is ConflictoCursoLocal -> "Hay un curso guardado que necesita asociación. Sus datos se conservan."
        is IOException -> "No hay conexión o no se pudo confirmar el resultado. Actualiza cuando vuelva la conexión para comprobarlo."
        else -> "No se pudo completar la operación. Los cursos guardados se conservan."
    }
}
