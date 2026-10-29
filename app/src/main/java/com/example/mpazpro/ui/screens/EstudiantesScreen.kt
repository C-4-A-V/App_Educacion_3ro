package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.ui.components.ImagenInteligente
import com.example.mpazpro.ui.components.SelectorFotoEstudiante
import com.example.mpazpro.ui.viewmodel.EstudiantesViewModel

@Composable
fun EstudiantesScreen(
    cursoId: Long,
    docenteId: Long,
    onVolver: () -> Unit,
    onGestionarActividades: () -> Unit,
    modifier: Modifier = Modifier,
    estudiantesViewModel: EstudiantesViewModel = viewModel()
) {
    val estado by estudiantesViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cursoId, docenteId) {
        estudiantesViewModel.cargar(cursoId, docenteId)
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
            TextButton(onClick = onVolver) {
                Text("Volver a mis cursos")
            }
        }

        item {
            Text(
                text = estado.curso?.nombre ?: "Curso",
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
            item { Text(mensaje) }
        }

        if (estado.curso != null && !estado.cargando) {
            // Menú moderno en tarjetas / burbujas visuales
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = onGestionarActividades,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "📚",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Actividades",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Crear y revisar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "👥",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Estudiantes",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${estado.estudiantes.size} registrados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Registrar nuevo estudiante",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Text("Usa nombres ficticios para las pruebas de demostración.")
            }

            item {
                SelectorFotoEstudiante(
                    fotoRuta = estado.fotoRuta,
                    errorFoto = estado.errorFoto,
                    habilitado = !estado.guardando && !estado.importandoFoto,
                    onSeleccion = estudiantesViewModel::seleccionarFoto,
                    onError = estudiantesViewModel::mostrarErrorFoto
                )
            }

            if (estado.importandoFoto) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Preparando fotografía…")
                }
            }

            item {
                OutlinedTextField(
                    value = estado.nombre,
                    onValueChange = estudiantesViewModel::actualizarNombre,
                    label = { Text("Nombre del estudiante") },
                    singleLine = true,
                    enabled = !estado.guardando && !estado.importandoFoto,
                    isError = estado.errorNombre != null,
                    supportingText = {
                        estado.errorNombre?.let { Text(it) }
                    },
                    trailingIcon = {
                        if (estado.errorNombre != null) {
                            Icon(
                                painter = painterResource(
                                    android.R.drawable.ic_dialog_alert
                                ),
                                contentDescription = "Error en el nombre",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = estudiantesViewModel::registrar,
                    enabled = !estado.guardando && !estado.importandoFoto,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (estado.guardando) "Guardando…"
                        else "Guardar estudiante"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Listado de estudiantes (${estado.estudiantes.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (estado.estudiantes.isEmpty()) {
                item {
                    Text("Todavía no hay estudiantes registrados en este curso.")
                }
            }

            items(
                items = estado.estudiantes,
                key = { it.id }
            ) { estudiante ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        ImagenInteligente(
                            ruta = estudiante.fotoRuta,
                            descripcion = "Fotografía de ${estudiante.nombre}"
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = estudiante.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "ID: E-${estudiante.id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}
