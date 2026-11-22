package com.example.mpazpro.ui.viewmodel

import android.app.Application
import android.net.Uri
import com.example.mpazpro.data.media.FotoStorage
import com.example.mpazpro.data.repository.PreferenciasEstudiante
import com.example.mpazpro.data.repository.PreferenciasPerfilEstudiante
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.local.DatosDemostracion
import com.example.mpazpro.data.repository.EstudianteRepository
import com.example.mpazpro.model.AccesoEstudianteUiState
import com.example.mpazpro.model.ProgresoEstudiante
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccesoEstudianteViewModel(
    application: Application,
    private val savedState: androidx.lifecycle.SavedStateHandle
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)

    private val repository = EstudianteRepository(
        estudianteDao = database.estudianteDao(),
        cursoDao = database.cursoDao(),
        actividadDao = database.actividadDao()
    )

    private val _uiState = MutableStateFlow(AccesoEstudianteUiState())
    val uiState = _uiState.asStateFlow()

    private var progresoJob: Job? = null
    private val preferencias = PreferenciasEstudiante(application)
    private val fotos = FotoStorage(application)

    init {
        cargarPerfiles()
    }

    private fun cargarPerfiles() {
        viewModelScope.launch {
            try {
                DatosDemostracion.preparar(database)
                repository.observarPerfiles().collect { perfiles ->
                    val idActual = _uiState.value.seleccionado?.id ?: savedState.get<Long>("estudianteId")
                    if (idActual != null && perfiles.none { it.id == idActual }) {
                        savedState.remove<Long>("estudianteId")
                        progresoJob?.cancel()
                        progresoJob = null
                        _uiState.update { it.copy(progreso = ProgresoEstudiante(), paraContinuar = null,
                            mensajesDocente = emptyList()) }
                    }
                    _uiState.update { actual ->
                        actual.copy(
                            perfiles = perfiles,
                            cargando = false,
                            // Actualiza el perfil elegido si cambian sus datos.
                            seleccionado = perfiles.find { it.id == (actual.seleccionado?.id ?: savedState.get<Long>("estudianteId")) },
                            mensaje = null
                        )
                    }
                    if (progresoJob == null) {
                        _uiState.value.seleccionado?.let { seleccionar(it.id) }
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensaje = "No se pudieron cargar los perfiles."
                    )
                }
            }
        }
    }

    fun ingresar(rut: String, pin: String, alIngresar: () -> Unit) {
        if (_uiState.value.ingresando) return
        _uiState.update { it.copy(ingresando = true, mensaje = null) }
        viewModelScope.launch {
            try {
                val id = repository.autenticar(rut, pin)
                if (id == null) {
                    _uiState.update { it.copy(ingresando = false, mensaje = "Revisa tu RUT y PIN. Si no recuerdas tu PIN, pide ayuda a tu docente. Después de varios intentos, espera un minuto.") }
                } else {
                    seleccionar(id)
                    _uiState.update { it.copy(ingresando = false) }
                    alIngresar()
                }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                _uiState.update { it.copy(ingresando = false, mensaje = "No pudimos ingresar. Inténtalo nuevamente.") }
            }
        }
    }

    private fun seleccionar(estudianteId: Long) {
        val perfil = _uiState.value.perfiles.find {
            it.id == estudianteId
        } ?: return

        savedState["estudianteId"] = estudianteId
        _uiState.update { it.copy(seleccionado = perfil, progreso = ProgresoEstudiante(), paraContinuar = null, mensajesDocente = emptyList(), mensaje = null,
            preferencias = PreferenciasPerfilEstudiante(), preferenciasCargadas = false,
            guardandoPerfil = false, mensajePerfil = null) }

        progresoJob?.cancel()
        progresoJob = viewModelScope.launch {
            try {
                combine(
                    repository.observarProgreso(estudianteId),
                    database.actividadDao().observarParaContinuar(estudianteId),
                    preferencias.observar(estudianteId),
                    database.mensajeDocenteDao().observar(estudianteId)
                ) { progreso, pendiente, ajustes, mensajes ->
                    { estado: AccesoEstudianteUiState ->
                        estado.copy(progreso = progreso, paraContinuar = pendiente,
                            preferencias = ajustes, preferenciasCargadas = true, mensajesDocente = mensajes)
                    }
                }.collect { actualizar ->
                    _uiState.update(actualizar)
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(mensaje = "No se pudo actualizar tu progreso. Vuelve a seleccionar tu perfil.") }
            }
        }
    }

    private fun guardarPerfil(accion: suspend (Long) -> Unit) {
        val id = _uiState.value.seleccionado?.id ?: return
        if (_uiState.value.guardandoPerfil || !_uiState.value.preferenciasCargadas) return
        _uiState.update { it.copy(guardandoPerfil = true, mensajePerfil = null) }
        viewModelScope.launch {
            try {
                accion(id)
                if (_uiState.value.seleccionado?.id == id) {
                    _uiState.update { it.copy(guardandoPerfil = false, mensajePerfil = "¡Listo! Tus cambios están guardados.") }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (_uiState.value.seleccionado?.id == id) {
                    _uiState.update { it.copy(guardandoPerfil = false, mensajePerfil = "No pudimos guardar el cambio. Inténtalo otra vez.") }
                }
            }
        }
    }

    fun cambiarPaleta(valor: com.example.mpazpro.data.repository.PaletaEstudiante) =
        guardarPerfil { preferencias.guardarPaleta(it, valor) }
    fun cambiarOscuro(valor: Boolean) = guardarPerfil { preferencias.guardarOscuro(it, valor) }
    fun cambiarTextoGrande(valor: Boolean) = guardarPerfil { preferencias.guardarTextoGrande(it, valor) }
    fun cambiarObjetivo(valor: String) = guardarPerfil { preferencias.guardarObjetivo(it, valor) }

    fun guardarFotoPerfil(uri: Uri) = guardarPerfil { id ->
        val ruta = fotos.importar(uri)
        check(database.estudianteDao().actualizarFotoPerfil(id, ruta) == 1)
    }

    fun marcarMensajeLeido(id: Long) {
        val estudianteId = _uiState.value.seleccionado?.id ?: return
        viewModelScope.launch {
            try {
                database.mensajeDocenteDao().marcarLeido(id, estudianteId)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                _uiState.update { it.copy(mensaje = "No se pudo guardar la lectura del mensaje.") }
            }
        }
    }

    fun mostrarErrorPerfil(mensaje: String) {
        _uiState.update { it.copy(mensajePerfil = mensaje) }
    }

    fun salir() {
        savedState.remove<Long>("estudianteId")
        progresoJob?.cancel()
        progresoJob = null
        _uiState.update { it.copy(seleccionado = null, progreso = ProgresoEstudiante(), paraContinuar = null, mensajesDocente = emptyList(),
            preferencias = PreferenciasPerfilEstudiante(), preferenciasCargadas = false,
            guardandoPerfil = false, mensajePerfil = null) }
    }
}
