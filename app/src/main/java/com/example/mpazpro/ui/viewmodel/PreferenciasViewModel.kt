package com.example.mpazpro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.repository.PreferenciasLectura
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreferenciasViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencias = PreferenciasLectura(application)
    private val _lectura = MutableStateFlow(false)
    val lectura = _lectura.asStateFlow()
    init {
        viewModelScope.launch {
            try {
                preferencias.modoLectura.collect { _lectura.value = it }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: java.io.IOException) {
                android.util.Log.w("Preferencias", "No se pudo leer el modo de lectura", error)
            }
        }
    }
}
