package com.example.mpazpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.repository.AprendeRepository
import com.example.mpazpro.data.repository.DatoPudu
import com.example.mpazpro.data.repository.TemaPudu
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AprendePuduEstado(
    val tema: TemaPudu = TemaPudu.TODOS,
    val dato: DatoPudu? = null,
    val cargando: Boolean = false,
    val usandoDatoLocal: Boolean = false
)

class AprendePuduViewModel : ViewModel() {
    private val repository = AprendeRepository()
    private val _estado = MutableStateFlow(AprendePuduEstado())
    val estado = _estado.asStateFlow()
    private var consulta: Job? = null
    private var reserva: Pair<TemaPudu, String>? = null
    private val bolsa = BolsaCuriosidades()

    init { siguiente() }

    fun elegirTema(tema: TemaPudu) {
        if (_estado.value.tema == tema) return
        _estado.value = _estado.value.copy(tema = tema, dato = null)
        siguiente()
    }

    fun siguiente() {
        consulta?.cancel()
        reserva?.let { (temaReservado, id) -> bolsa.devolver(temaReservado, id) }
        reserva = null
        val tema = _estado.value.tema
        val locales = repository.datosLocales.filter { tema == TemaPudu.TODOS || it.tema == tema }
        val candidatos = locales.map { it.id } +
            if (tema == TemaPudu.CHILE || tema == TemaPudu.TODOS) listOf("capital") else emptyList()
        val elegido = bolsa.siguiente(tema, candidatos)
        reserva = tema to elegido
        consulta = viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, usandoDatoLocal = false)
            val dato = if (elegido == "capital") {
                try {
                    repository.capitalDeChile()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    _estado.value = _estado.value.copy(usandoDatoLocal = true)
                    val idLocal = bolsa.siguiente(tema, candidatos, excluir = setOf("capital"))
                    locales.first { it.id == idLocal }
                }
            } else {
                locales.first { it.id == elegido }
            }
            reserva = null
            bolsa.mostrado(tema, dato.id)
            _estado.value = _estado.value.copy(dato = dato, cargando = false)
        }
    }
}
