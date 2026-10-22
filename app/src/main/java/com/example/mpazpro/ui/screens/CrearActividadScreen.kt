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
import com.example.mpazpro.ui.viewmodel.CrearActividadViewModel

@Composable
fun CrearActividadScreen(
    cursoId: Long,
    docenteId: Long,
    asignatura: Asignatura,
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    modifier: Modifier = Modifier,
    actividadViewModel: CrearActividadViewModel = viewModel()
) {
    val estado by actividadViewModel.uiState
        .collectAsStateWithLifecycle()

    val formulario = estado.formulario

    val habilitado =
        !estado.cargando &&
                !estado.guardando &&
                !estado.guardado

    LaunchedEffect(cursoId, docenteId) {
        actividadViewModel.cargar(
            cursoId = cursoId,
            docenteId = docenteId
        )
    }

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            onGuardado()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TextButton(
                onClick = onVolver,
                enabled = !estado.guardando
            ) {
                Text("Volver a las actividades")
            }
        }

        item {
            Text(
                text = "Crear actividad",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (estado.cargando || estado.guardando) {
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

        val curso = estado.curso

        if (curso != null && !estado.cargando) {
            item {
                Text(
                    text = curso.nombre,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = asignatura.titulo,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                CampoActividad(
                    valor = formulario.titulo,
                    etiqueta = "Título de la actividad",
                    habilitado = habilitado,
                    onCambio = actividadViewModel::actualizarTitulo
                )
            }

            item {
                CampoActividad(
                    valor = formulario.instrucciones,
                    etiqueta = "Instrucciones para el estudiante",
                    habilitado = habilitado,
                    multilinea = true,
                    onCambio = actividadViewModel::actualizarInstrucciones
                )
            }

            item {
                CampoActividad(
                    valor = formulario.pregunta,
                    etiqueta = "Pregunta",
                    habilitado = habilitado,
                    multilinea = true,
                    onCambio = actividadViewModel::actualizarPregunta
                )
            }

            item {
                Text(
                    text = "Alternativas",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Completa las cuatro y marca la respuesta correcta.",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    formulario.alternativas.forEachIndexed {
                            indice, alternativa ->

                        val letra = "ABCD"[indice]

                        Column {
                            CampoActividad(
                                valor = alternativa,
                                etiqueta = "Alternativa $letra",
                                habilitado = habilitado,
                                onCambio = { valor ->
                                    actividadViewModel.actualizarAlternativa(
                                        indice = indice,
                                        valor = valor
                                    )
                                }
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = formulario.respuestaCorrecta == indice,
                                        enabled = habilitado,
                                        role = Role.RadioButton,
                                        onClick = {
                                            actividadViewModel.seleccionarRespuesta(
                                                indice
                                            )
                                        }
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = formulario.respuestaCorrecta == indice,
                                    onClick = null,
                                    enabled = habilitado
                                )

                                Text(
                                    text = "Marcar $letra como correcta",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        actividadViewModel.guardar(asignatura.id)
                    },
                    enabled = habilitado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (estado.guardando) {
                            "Guardando…"
                        } else {
                            "Guardar actividad"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CampoActividad(
    valor: String,
    etiqueta: String,
    habilitado: Boolean,
    multilinea: Boolean = false,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = {
            Text(etiqueta)
        },
        enabled = habilitado,
        singleLine = !multilinea,
        minLines = if (multilinea) 2 else 1,
        maxLines = if (multilinea) 5 else 1,
        modifier = Modifier.fillMaxWidth()
    )
}