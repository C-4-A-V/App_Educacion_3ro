package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.DocenteRepository
import com.example.mpazpro.data.repository.ResultadoRegistroDocente
import com.example.mpazpro.model.RegistroDocenteErrores
import com.example.mpazpro.model.RegistroDocenteUiState
import com.example.mpazpro.validation.RegistroDocenteValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistroDocenteViewModel(
    application: Application
) : AndroidViewModel(application) {

    // Usamos el contexto de la aplicación para obtener Room.
    // No guardamos una referencia a la Activity.
    private val repository = DocenteRepository(
        MpazDatabase.obtenerInstancia(application).docenteDao()
    )

    private val _uiState = MutableStateFlow(RegistroDocenteUiState())
    val uiState = _uiState.asStateFlow()

    fun actualizarNombre(valor: String) {
        actualizar { it.copy(nombre = valor) }
    }

    fun actualizarCorreo(valor: String) {
        actualizar { it.copy(correo = valor) }
    }

    fun actualizarClave(valor: String) {
        actualizar { it.copy(clave = valor) }
    }

    fun actualizarConfirmacion(valor: String) {
        actualizar { it.copy(confirmacion = valor) }
    }

    // Centraliza la actualización y la validación de los campos.
    private fun actualizar(
        cambio: (RegistroDocenteUiState) -> RegistroDocenteUiState
    ) {
        _uiState.update { actual ->
            if (actual.guardando || actual.registrado) {
                actual
            } else {
                val nuevo = cambio(actual).copy(mensaje = null)

                nuevo.copy(
                    errores = if (nuevo.intentoValidar) {
                        RegistroDocenteValidator.validar(nuevo)
                    } else {
                        RegistroDocenteErrores()
                    }
                )
            }
        }
    }

    fun registrar() {
        val actual = _uiState.value

        // Evita enviar el formulario varias veces.
        if (actual.guardando || actual.registrado) return

        val errores = RegistroDocenteValidator.validar(actual)

        _uiState.update {
            it.copy(
                errores = errores,
                intentoValidar = true,
                mensaje = null
            )
        }

        val hayErrores = listOf(
            errores.nombre,
            errores.correo,
            errores.clave,
            errores.confirmacion
        ).any { it != null }

        if (hayErrores) return

        _uiState.update { it.copy(guardando = true) }

        viewModelScope.launch {
            try {
                when (
                    repository.registrar(
                        nombre = actual.nombre,
                        correo = actual.correo,
                        clave = actual.clave
                    )
                ) {
                    is ResultadoRegistroDocente.Registrado -> {
                        _uiState.update {
                            it.copy(
                                guardando = false,
                                registrado = true,
                                clave = "",
                                confirmacion = "",
                                mensaje = "Cuenta creada y guardada en este dispositivo."
                            )
                        }
                    }

                    ResultadoRegistroDocente.CorreoExistente -> {
                        _uiState.update {
                            it.copy(
                                guardando = false,
                                errores = it.errores.copy(
                                    correo = "Este correo ya está registrado"
                                )
                            )
                        }
                    }
                }
            } catch (cancelacion: CancellationException) {
                // La cancelación de una corrutina no es un error del formulario.
                throw cancelacion
            } catch (error: Exception) {
                android.util.Log.e("RegistroDocenteVM", "Error al registrar docente", error)
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje = "No se pudo guardar la cuenta. Intenta nuevamente."
                    )
                }
            }
        }
    }
}
