package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.model.PerfilEstudiante
import com.example.mpazpro.ui.viewmodel.ResolverActividadViewModel

@Composable
fun ResolverActividadScreen(
    actividadId: Long,
    estudiante: PerfilEstudiante,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    actividadViewModel: ResolverActividadViewModel = viewModel()
) {
    val estado by actividadViewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(estudiante.id, actividadId) {
        actividadViewModel.cargar(
            estudianteId = estudiante.id,
            actividadId = actividadId
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            TextButton(
                onClick = onVolver,
                enabled = !estado.enviando
            ) {
                Text("Volver a las actividades")
            }
        }

        if (estado.cargando || estado.enviando) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        estado.mensaje?.let { mensaje ->
            item {
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        val actividad = estado.actividad

        if (actividad != null && !estado.cargando) {
            val alternativas = listOf(
                actividad.alternativaA,
                actividad.alternativaB,
                actividad.alternativaC,
                actividad.alternativaD
            )

            val asignatura = Asignatura.buscarPorId(
                actividad.asignaturaId
            )

            val puedeResponder =
                !estado.enviando && estado.intento == null

            item {
                Text(
                    text = actividad.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Text(
                    text = asignatura?.titulo ?: "Actividad",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = estudiante.cursoNombre,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = actividad.instrucciones,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            item {
                Text(
                    text = actividad.pregunta,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (estado.intento == null) {
                item {
                    Text(
                        text = "Esta actividad permite un intento. Revisa tu respuesta antes de enviarla."
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    alternativas.forEachIndexed { indice, texto ->
                        val seleccionada =
                            estado.respuestaSeleccionada == indice

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = seleccionada,
                                        enabled = puedeResponder,
                                        role = Role.RadioButton,
                                        onClick = {
                                            actividadViewModel.seleccionarRespuesta(
                                                indice
                                            )
                                        }
                                    )
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = seleccionada,
                                    onClick = null,
                                    enabled = puedeResponder
                                )

                                Text(
                                    text = "${"ABCD"[indice]}. $texto",
                                    modifier = Modifier
                                        .padding(start = 12.dp)
                                        .weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            val intento = estado.intento

            if (intento == null) {
                item {
                    Button(
                        onClick = actividadViewModel::enviarRespuesta,
                        enabled = !estado.enviando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (estado.enviando) {
                                "Guardando respuesta…"
                            } else {
                                "Enviar respuesta"
                            }
                        )
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Resultado",
                                style = MaterialTheme.typography.titleLarge
                            )

                            Text(
                                text = if (intento.correcta) {
                                    "¡Muy bien! Tu respuesta es correcta."
                                } else {
                                    "Buen intento. Revisa la respuesta correcta."
                                },
                                color = if (intento.correcta) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )

                            Text(
                                text = "Tu respuesta: ${
                                    alternativas.getOrNull(
                                        intento.respuestaElegida
                                    ) ?: "No disponible"
                                }"
                            )

                            Text(
                                text = "Respuesta correcta: ${
                                    alternativas.getOrNull(
                                        intento.respuestaCorrecta
                                    ) ?: "No disponible"
                                }"
                            )

                            Text(
                                text = "Tu resultado quedó guardado.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onVolver,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver a las actividades")
                    }
                }
            }
        }
    }
}
