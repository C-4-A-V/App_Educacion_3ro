package com.example.mpazpro.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.DocenteEntity
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.media.FotoStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilDocenteUiState(
    val docente: DocenteEntity? = null,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val mensaje: String? = null
)

class PerfilDocenteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dao = MpazDatabase
        .obtenerInstancia(application)
        .docenteDao()

    private val fotos = FotoStorage(application)

    private val _uiState = MutableStateFlow(
        PerfilDocenteUiState()
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
        _uiState.value = PerfilDocenteUiState()

        consulta = viewModelScope.launch {
            try {
                dao.observarPorId(docenteId).collect { docente ->
                    _uiState.update {
                        it.copy(
                            docente = docente,
                            cargando = false,
                            mensaje = if (docente == null) {
                                "Perfil no disponible."
                            } else {
                                it.mensaje
                            }
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensaje = "No se pudo cargar el perfil."
                    )
                }
            }
        }
    }

    fun mostrarError(texto: String) {
        _uiState.update {
            it.copy(mensaje = texto)
        }
    }

    fun guardarFoto(uri: Uri) {
        val docenteId = _uiState.value.docente?.id ?: return

        if (_uiState.value.guardando) return

        _uiState.update {
            it.copy(
                guardando = true,
                mensaje = null
            )
        }

        viewModelScope.launch {
            try {
                val ruta = fotos.importar(uri)

                check(
                    dao.actualizarFoto(docenteId, ruta) == 1
                )

                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje = "Fotografía actualizada."
                    )
                }

            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje = "No se pudo guardar la foto. " +
                            "Usa una imagen de hasta 10 MB."
                    )
                }
            }
        }
    }
}
