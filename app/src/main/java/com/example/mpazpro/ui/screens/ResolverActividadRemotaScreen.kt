package com.example.mpazpro.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.model.PerfilEstudiante
import com.example.mpazpro.ui.viewmodel.ResolverActividadRemotaViewModel

@Composable
fun ResolverActividadRemotaScreen(
    actividadId: Long,
    estudiante: PerfilEstudiante,
    onVolver: () -> Unit,
    onSiguiente: (Long) -> Unit = {},
    modelo: ResolverActividadRemotaViewModel = viewModel()
) {
    val estado by modelo.uiState.collectAsStateWithLifecycle()
    var avanzando by remember(estudiante.id, actividadId) { mutableStateOf(false) }
    LaunchedEffect(estudiante.id, actividadId) { modelo.cargar(estudiante.id, actividadId) }
    ResolverActividadContenido(
        estado = estado,
        onVolver = onVolver,
        onSeleccionar = modelo::seleccionarRespuesta,
        onEnviar = modelo::enviarRespuesta,
        onReintentar = modelo::reintentar,
        onRecargar = modelo::recargar,
        onReintentarContinuacion = modelo::actualizarContinuacion,
        onSiguiente = {
            if (!avanzando) estado.siguienteActividadId?.let {
                avanzando = true
                onSiguiente(it)
            }
        },
        mostrarProgreso = false,
        mostrarEstrella = false
    )
}
