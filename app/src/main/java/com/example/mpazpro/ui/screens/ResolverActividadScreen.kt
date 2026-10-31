package com.example.mpazpro.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import com.example.mpazpro.ui.components.FeedbackAnimado
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    actividadViewModel: ResolverActividadViewModel = viewModel()
) {
    val estado by actividadViewModel.uiState.collectAsStateWithLifecycle()
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
        onRecargar = actividadViewModel::recargar
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
    onRecargar: () -> Unit = {}
) {
    AventuraTheme {
        Surface(modifier.fillMaxSize().anchoFormulario()) {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    TextButton(onClick = onVolver, enabled = !estado.enviando) {
                        Text("← Volver a las actividades")
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
                                                enabled = respondiendo && !estado.enviando,
                                                role = Role.RadioButton,
                                                onClick = { onSeleccionar(indice) }
                                            ).heightIn(min = 64.dp).padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(selected = seleccionada, onClick = null, enabled = respondiendo && !estado.enviando)
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
                                    PuduExplorador(Modifier.size(80.dp))
                                    Text(
                                        when {
                                            intento.correcta -> "¡Lo lograste! Ganaste una estrella ⭐"
                                            !intento.finalizada -> "¡Buen esfuerzo! Probemos otra vez."
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
                        item {
                            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                                Text("Volver a las actividades")
                            }
                        }
                    }
                }
            }
        }
    }
}
