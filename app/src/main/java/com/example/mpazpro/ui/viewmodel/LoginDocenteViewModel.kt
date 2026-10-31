package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.local.DatosDemostracion
import com.example.mpazpro.data.repository.DocenteRepository
import com.example.mpazpro.model.LoginDocenteErrores
import com.example.mpazpro.model.LoginDocenteUiState
import com.example.mpazpro.validation.LoginDocenteValidator
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginDocenteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)
    private val repository = DocenteRepository(database.docenteDao())

    private val _uiState = MutableStateFlow(LoginDocenteUiState())
    val uiState = _uiState.asStateFlow()

    private var ingresoJob: Job? = null

    fun actualizarCorreo(valor: String) {
        // Transforma automáticamente a minúsculas cualquier correo ingresado
        actualizar { it.copy(correo = valor.lowercase(Locale.ROOT)) }
    }

    fun actualizarClave(valor: String) {
        actualizar { it.copy(clave = valor) }
    }

    private fun actualizar(
        cambio: (LoginDocenteUiState) -> LoginDocenteUiState
    ) {
        _uiState.update { actual ->
            if (actual.cargando || actual.docente != null) {
                actual
            } else {
                val nuevo = cambio(actual).copy(mensaje = null)

                nuevo.copy(
                    errores = if (nuevo.intentoValidar) {
                        LoginDocenteValidator.validar(
                            nuevo.correo,
                            nuevo.clave
                        )
                    } else {
                        LoginDocenteErrores()
                    }
                )
            }
        }
    }

    fun iniciarSesion() {
        val actual = _uiState.value

        if (actual.cargando || actual.docente != null) return

        // Limpiamos espacios accidentales en el correo y clave
        val correoLimpio = actual.correo.trim().lowercase(Locale.ROOT)
        val claveLimpia = actual.clave

        val errores = LoginDocenteValidator.validar(
            correoLimpio,
            claveLimpia
        )

        _uiState.update {
            it.copy(
                correo = correoLimpio,
                errores = errores,
                intentoValidar = true,
                mensaje = null
            )
        }

        if (errores.correo != null || errores.clave != null) return

        _uiState.update { it.copy(cargando = true) }

        ingresoJob = viewModelScope.launch {
            try {
                DatosDemostracion.preparar(database)
                val docente = repository.autenticar(
                    correo = correoLimpio,
                    clave = claveLimpia
                )

                _uiState.update {
                    it.copy(
                        cargando = false,
                        clave = "",
                        docente = docente,
                        mensaje = if (docente == null) {
                            "Correo o contraseña incorrectos"
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
                        clave = "",
                        mensaje = "No se pudo acceder. Intenta nuevamente."
                    )
                }
            }
        }
    }

    fun cerrarSesion() {
        // También permite cancelar un acceso todavía en curso.
        ingresoJob?.cancel()
        ingresoJob = null
        _uiState.value = LoginDocenteUiState()
    }
}
