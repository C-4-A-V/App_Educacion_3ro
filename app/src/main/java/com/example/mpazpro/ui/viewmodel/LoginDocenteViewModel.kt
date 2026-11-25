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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginDocenteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)
    private val repository = DocenteRepository(database.docenteDao())

    private val _uiState = MutableStateFlow(LoginDocenteUiState())
    val uiState = _uiState.asStateFlow()

    val sesionRemota = com.example.mpazpro.data.repository.SesionDocenteRemota.repository.sesion
    private var ingresoJob: Job? = null

    fun iniciarSesionRemota() {
        val actual = _uiState.value
        if (actual.cargando || actual.docente != null) return
        val errores = LoginDocenteValidator.validar(actual.correo.trim(), actual.clave)
        if (errores.correo != null || errores.clave != null || actual.clave.length < 10) {
            _uiState.update { it.copy(errores = if (actual.clave.length < 10)
                errores.copy(clave = "Usa al menos 10 caracteres para el servidor") else errores,
                intentoValidar = true) }
            return
        }
        _uiState.update { it.copy(cargando = true, mensaje = null) }
        ingresoJob = viewModelScope.launch {
            try {
                val remoto = com.example.mpazpro.data.repository.SesionDocenteRemota.repository
                remoto.entrar(actual.correo, actual.clave)
                if (actual.recuperarAnterior) {
                    _uiState.update { it.copy(cargando = false, clave = "", recuperacionNecesaria = true) }
                    return@launch
                }
                val docente = com.example.mpazpro.data.repository.ResolverIdentidadDocente(
                    database, remoto, com.example.mpazpro.data.remote.ServicioActividadesServidor.api
                ).resolver()
                _uiState.update { it.copy(cargando = false, clave = "", docente = docente) }
            } catch (e: CancellationException) { throw e }
            catch (_: com.example.mpazpro.data.repository.RecuperacionDocenteNecesaria) {
                _uiState.update { it.copy(cargando = false, clave = "", recuperacionNecesaria = true) }
            }
            catch (e: com.example.mpazpro.data.repository.SesionDocenteExpirada) {
                _uiState.update { it.copy(cargando = false, clave = "", mensaje = "Correo o contraseña incorrectos.") }
            } catch (_: Exception) {
                _uiState.update { it.copy(cargando = false, clave = "", mensaje = "No pudimos conectar con el servidor.") }
            }
        }
    }

    fun recuperarDatos(correoLocal: String, claveLocal: String) {
        if (_uiState.value.cargando) return
        _uiState.update { it.copy(cargando = true, mensaje = null) }
        viewModelScope.launch {
            try {
                val id = com.example.mpazpro.data.repository.RecuperarDatosDocente(
                    database, com.example.mpazpro.data.repository.SesionDocenteRemota.repository,
                    com.example.mpazpro.data.remote.ServicioActividadesServidor.api
                ).recuperar(correoLocal, claveLocal)
                val perfil = database.docenteDao().observarPorId(id).first()
                    ?: error("Perfil no disponible")
                _uiState.update { it.copy(cargando = false, recuperacionNecesaria = false,
                    docente = com.example.mpazpro.model.DocenteSesion(perfil.id, perfil.nombre, perfil.correo)) }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                _uiState.update { it.copy(cargando = false,
                    mensaje = "No se pudieron recuperar los datos. Revisa las credenciales y la conexión.") }
            }
        }
    }

    fun cerrarSesionRemota() {
        ingresoJob?.cancel()
        _uiState.value = LoginDocenteUiState()
        viewModelScope.launch {
            try { com.example.mpazpro.data.repository.SesionDocenteRemota.repository.salir() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { /* El repositorio limpia la sesión incluso sin conexión. */ }
        }
    }

    fun solicitarRecuperacion(valor: Boolean) {
        actualizar { it.copy(recuperarAnterior = valor) }
    }

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
        cerrarSesionRemota()
    }
}
