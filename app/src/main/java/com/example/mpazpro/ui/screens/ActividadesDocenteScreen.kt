package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.ui.viewmodel.ActividadesDocenteViewModel

@Composable
fun ActividadesDocenteScreen(
    cursoId: Long,
    docenteId: Long,
    asignatura: Asignatura,
    onVolver: () -> Unit,
    onCrearActividad: () -> Unit,
    onVerResultados: (Long) -> Unit,
    soloResultados: Boolean = false,
    modifier: Modifier = Modifier,
    actividadesViewModel: ActividadesDocenteViewModel = viewModel()
) {
    val estado by actividadesViewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(cursoId, asignatura.id, docenteId) {
        actividadesViewModel.cargar(
            cursoId = cursoId,
            asignaturaId = asignatura.id,
            docenteId = docenteId
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TextButton(onClick = onVolver) {
                Text("Volver")
            }
        }

        item {
            Text(
                text = if (soloResultados) {
                    "Ver resultados"
                } else {
                    "Actividades"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (estado.cargando) {
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

        if (
            curso != null &&
            !estado.cargando &&
            estado.mensaje == null
        ) {
            item {
                Text(
                    text = asignatura.titulo,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Curso: ${curso.nombre}",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (!soloResultados) {
                item {
                    Button(
                        onClick = onCrearActividad,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear actividad")
                    }
                }
            }

            item {
                Text(
                    text = "Actividades guardadas: ${estado.actividades.size}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (estado.actividades.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Sin actividades",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = if (soloResultados) {
                                    "Todavía no hay actividades en esta asignatura."
                                } else {
                                    "Crea la primera actividad para esta asignatura."
                                }
                            )
                        }
                    }
                }
            }

            items(
                items = estado.actividades,
                key = { it.id }
            ) { actividad ->
                Card(
                    onClick = {
                        onVerResultados(actividad.id)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = actividad.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = actividad.instrucciones,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = actividad.pregunta,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "Toca para ver resultados de los estudiantes",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
