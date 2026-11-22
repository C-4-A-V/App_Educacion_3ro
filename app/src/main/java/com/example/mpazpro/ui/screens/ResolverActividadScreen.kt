package com.example.mpazpro.ui.screens

import com.example.mpazpro.ui.components.BotonVolver

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import com.example.mpazpro.ui.components.FeedbackAnimado
import com.example.mpazpro.ui.components.BotonSecundario
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.mpazpro.ui.components.AnimoPudu
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.mpazpro.ui.components.anchoFormulario
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.model.PerfilEstudiante
import com.example.mpazpro.model.ResolverActividadUiState
import com.example.mpazpro.ui.components.PuduExplorador
import com.example.mpazpro.ui.theme.*
import com.example.mpazpro.ui.viewmodel.ResolverActividadViewModel

@Composable
fun ResolverActividadScreen(
    actividadId: Long,
    estudiante: PerfilEstudiante,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    actividadViewModel: ResolverActividadViewModel = viewModel(),
    onSiguiente: (Long) -> Unit = {}
) {
    val estado by actividadViewModel.uiState.collectAsStateWithLifecycle()
    var avanzando by remember(estudiante.id, actividadId) { mutableStateOf(false) }
    LaunchedEffect(estudiante.id, actividadId) {
        actividadViewModel.cargar(estudiante.id, actividadId)
    }
    ResolverActividadContenido(
        estado = estado,
        onVolver = onVolver,
        onSeleccionar = actividadViewModel::seleccionarRespuesta,
        onEnviar = actividadViewModel::enviarRespuesta,
        onReintentar = actividadViewModel::reintentar,
        modifier = modifier,
        onRecargar = actividadViewModel::recargar,
        onSiguiente = {
            if (!avanzando) estado.siguienteActividadId?.let {
                avanzando = true
                onSiguiente(it)
            }
        },
        onRecargarProgreso = actividadViewModel::reintentarProgreso,
        onReintentarContinuacion = actividadViewModel::actualizarContinuacion
    )
}

@Composable
internal fun ResolverActividadContenido(
    estado: ResolverActividadUiState,
    onVolver: () -> Unit,
    onSeleccionar: (Int) -> Unit,
    onEnviar: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
    onRecargar: () -> Unit = {},
    onSiguiente: () -> Unit = {},
    onReintentarContinuacion: () -> Unit = {},
    onRecargarProgreso: () -> Unit = onRecargar,
    mostrarProgreso: Boolean = true,
    mostrarEstrella: Boolean = true
) {
    var pistaVisible by rememberSaveable(estado.actividad?.id) { mutableStateOf(false) }
    var celebrar by rememberSaveable(estado.actividad?.id) { mutableStateOf(false) }
    var avanceAnterior by remember(estado.actividad?.id) { mutableStateOf<Float?>(null) }
    LaunchedEffect(estado.progresoAsignatura) {
        val progreso = estado.progresoAsignatura ?: return@LaunchedEffect
        if (avanceAnterior != null && avanceAnterior!! < 1f && progreso.total > 0 && progreso.porcentaje >= 1f) {
            celebrar = true
        }
        avanceAnterior = progreso.porcentaje
    }
    val animoPudu = when {
        estado.progresoAsignatura?.porcentaje == 1f || estado.intento?.correcta == true -> AnimoPudu.Alegre
        pistaVisible || estado.intento?.correcta == false -> AnimoPudu.Pensando
        else -> AnimoPudu.Tranquilo
    }
    AventuraTheme {
        Scaffold(
            modifier = modifier.fillMaxSize().anchoFormulario(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!estado.cargando && estado.intento?.finalizada == true && !estado.reintentando) {
                    Surface(tonalElevation = 3.dp) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Tu avance está guardado.", style = MaterialTheme.typography.bodyMedium)
                            when {
                                estado.cargandoContinuacion -> LinearProgressIndicator(Modifier.fillMaxWidth())
                                estado.errorContinuacion -> {
                                    Text("No pudimos buscar la siguiente pregunta.")
                                    OutlinedButton(onClick = onReintentarContinuacion) { Text("Intentar nuevamente") }
                                }
                                estado.siguienteActividadId == null -> Button(
                                    onClick = onVolver,
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                                ) { Text("Terminar actividad") }
                                else -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedButton(onClick = onVolver, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                                        Text("Guardar hasta aquí", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                    Button(onClick = onSiguiente, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                                        Text("Siguiente pregunta", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { espacio ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(espacio),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    BotonVolver(onClick = onVolver, enabled = !estado.enviando) {
                        Text("Volver a las actividades")
                    }
                }
                if (mostrarProgreso && estado.actividad != null && !estado.cargando) {
                    item(key = "progreso_asignatura") {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PuduExplorador(Modifier.size(48.dp), animo = animoPudu)
                                Text("Tu avance en esta asignatura", style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
                            }
                            val progreso = estado.progresoAsignatura
                            when {
                                estado.errorProgreso -> {
                                    Text("No pudimos cargar tu avance.", textAlign = TextAlign.Center)
                                    BotonSecundario(onClick = onRecargarProgreso, enabled = !estado.enviando) { Text("Intentar nuevamente") }
                                }
                                progreso == null -> LinearProgressIndicator(Modifier.fillMaxWidth())
                                else -> {
                                    val avanceAnimado by animateFloatAsState(
                                        targetValue = progreso.porcentaje,
                                        animationSpec = tween(600),
                                        label = "Avance de la asignatura"
                                    )
                                    Text(
                                        "${progreso.completadas} de ${progreso.total} actividades completadas · ${progreso.porcentajeTexto}",
                                        style = MaterialTheme.typography.labelLarge,
                                        textAlign = TextAlign.Center
                                    )
                                    LinearProgressIndicator(
                                        progress = { avanceAnimado },
                                        modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(50)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                    Text(
                                        when {
                                            progreso.total == 0 -> "¡Pronto tendremos nuevas aventuras!"
                                            progreso.porcentaje >= 1f -> "¡Lo lograste! Completaste esta asignatura ⭐"
                                            progreso.porcentaje >= 0.7f -> "¡Ánimo, falta poquito!"
                                            progreso.porcentaje >= 0.4f -> "¡Ya avanzaste mucho! ¡Solo un poco más!"
                                            progreso.completadas > 0 -> "¡Vas súper bien, sigue así!"
                                            else -> "¡Comencemos esta aventura!"
                                        },
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                if (celebrar) {
                    item(key = "celebracion_asignatura") {
                        FeedbackAnimado {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                            ) {
                                Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    PuduExplorador(Modifier.size(64.dp), animo = AnimoPudu.Alegre)
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("¡Una aventura completada!", style = MaterialTheme.typography.titleLarge)
                                        Text("Terminaste todas las actividades de esta asignatura. ¡Tu esfuerzo merece una celebración!")
                                    }
                                }
                            }
                        }
                    }
                }
                if (estado.cargando || estado.enviando) {
                    item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                }
                estado.mensaje?.let { mensaje ->
                    item {
                        Column {
                            Text(mensaje, color = MaterialTheme.colorScheme.error)
                            if (estado.actividad == null && !estado.cargando) {
                                OutlinedButton(onClick = onRecargar) { Text("Intentar cargar nuevamente") }
                            }
                        }
                    }
                }
                val actividad = estado.actividad
                if (actividad != null && !estado.cargando) {
                    val asignatura = Asignatura.buscarPorId(actividad.asignaturaId)
                    val alternativas = listOf(actividad.alternativaA, actividad.alternativaB, actividad.alternativaC, actividad.alternativaD)
                    val intento = estado.intento
                    val respondiendo = intento == null || estado.reintentando
                    val usados = intento?.numero ?: 0
                    item {
                        Text(
                            if (actividad.maxIntentos == 3) "Práctica · hasta 3 intentos" else "Evaluación · 1 intento",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(actividad.titulo, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        Text(asignatura?.titulo.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = asignatura?.colorAventura() ?: MaterialTheme.colorScheme.primaryContainer,
                                contentColor = asignatura?.textoAventura() ?: MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Tu misión", style = MaterialTheme.typography.titleMedium)
                                Text(actividad.instrucciones, style = MaterialTheme.typography.bodyLarge)
                                Text(actividad.pregunta, style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                    if (actividad.maxIntentos == 3 && actividad.pista.isNotBlank() && intento?.finalizada != true) {
                        item(key = "ayuda_pregunta") {
                            Card(
                                modifier = Modifier.fillMaxWidth().animateContentSize(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    BotonSecundario(
                                        onClick = { pistaVisible = !pistaVisible },
                                        enabled = !estado.enviando
                                    ) { Text(if (pistaVisible) "Ocultar pista" else "¿Necesitas una pista?") }
                                    AnimatedVisibility(visible = pistaVisible) {
                                        Text(actividad.pista, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }
                    if (respondiendo) {
                        item {
                            Text(
                                if (actividad.maxIntentos == 3) "Te quedan ${actividad.maxIntentos - usados} oportunidades. ¡Tú puedes!"
                                else "Revisa tu respuesta: esta evaluación tiene un intento."
                            )
                        }
                    }
                    item {
                        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            alternativas.forEachIndexed { indice, texto ->
                                val seleccionada = estado.respuestaSeleccionada == indice
                                val fondo by animateColorAsState(
                                    if (seleccionada) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    label = "Alternativa seleccionada"
                                )
                                val borde by animateDpAsState(if (seleccionada) 3.dp else 1.dp, label = "Borde de selección")
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    color = fondo,
                                    contentColor = if (seleccionada) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    border = BorderStroke(
                                        borde,
                                        if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .selectable(
                                                selected = seleccionada,
                                                enabled = respondiendo && !estado.enviando && !estado.seleccionBloqueada,
                                                role = Role.RadioButton,
                                                onClick = { onSeleccionar(indice) }
                                            ).heightIn(min = 64.dp).padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(selected = seleccionada, onClick = null, enabled = respondiendo && !estado.enviando && !estado.seleccionBloqueada)
                                        Text("${"ABCD"[indice]}. $texto", modifier = Modifier.padding(start = 12.dp).weight(1f), style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }
                    if (respondiendo) {
                        item {
                            Button(
                                onClick = onEnviar,
                                enabled = !estado.enviando && estado.respuestaSeleccionada in 0..3,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                            ) {
                                Text(if (estado.enviando) "Guardando…" else "Enviar respuesta")
                            }
                        }
                    } else {
                        item(key = "resultado_${intento.numero}") {
                            FeedbackAnimado {
                            Card(shape = RoundedCornerShape(24.dp)) {
                                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    PuduExplorador(Modifier.size(80.dp), animo = animoPudu)
                                    Text(
                                        when {
                                            intento.correcta -> if (mostrarEstrella) "¡Lo lograste! Ganaste una estrella ⭐" else "¡Lo lograste! ¡Muy bien! ⭐"
                                            !intento.finalizada -> "Probemos otra estrategia. ¡Tú puedes!"
                                            else -> "¡Terminaste la actividad! Sigamos aprendiendo."
                                        },
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    if (intento.finalizada) {
                                        Text("Tu respuesta: ${alternativas.getOrNull(intento.respuestaElegida).orEmpty()}")
                                        Text("Respuesta correcta: ${alternativas.getOrNull(intento.respuestaCorrecta).orEmpty()}")
                                        if (estado.explicacion.isNotBlank()) {
                                            Text("Descubramos por qué", style = MaterialTheme.typography.titleMedium)
                                            Text(estado.explicacion, style = MaterialTheme.typography.bodyLarge)
                                        }
                                        Text("Tu resultado quedó guardado.", style = MaterialTheme.typography.bodyMedium)
                                        if (estado.progresoAsignatura?.porcentaje == 1f) {
                                            Text("¡Completaste esta asignatura! Gracias por tu esfuerzo.", style = MaterialTheme.typography.titleMedium)
                                        }
                                    } else {
                                        Text("Lee la pregunta con calma. Te quedan ${actividad.maxIntentos - usados} oportunidades.")
                                        Button(
                                            onClick = onReintentar,
                                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                                        ) { Text("Intentar otra vez") }
                                    }
                                }
                            }
                        }
                        }
                    }
                }
            }
        }
    }
}
