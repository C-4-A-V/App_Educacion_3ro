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
import com.example.mpazpro.model.PerfilEstudiante
import com.example.mpazpro.ui.viewmodel.ActividadesEstudianteViewModel

@Composable
fun AsignaturaScreen(
    asignatura: Asignatura,
    estudiante: PerfilEstudiante,
    onVolver: () -> Unit,
    onAbrirActividad: (Long) -> Unit,
    modifier: Modifier = Modifier,
    actividadesViewModel: ActividadesEstudianteViewModel = viewModel()
) {
    val estado by actividadesViewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(estudiante.id, asignatura.id) {
        actividadesViewModel.cargar(
            estudianteId = estudiante.id,
            asignaturaId = asignatura.id
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
                Text("Volver a mis asignaturas")
            }
        }

        item {
            Text(
                text = asignatura.titulo,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Text(
                text = estudiante.cursoNombre,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = asignatura.descripcion,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Text(
                text = "Mis actividades",
                style = MaterialTheme.typography.titleLarge
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

        if (
            !estado.cargando &&
            estado.mensaje == null &&
            estado.actividades.isEmpty()
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Sin actividades asignadas",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Aquí aparecerán las actividades que tu docente prepare para esta asignatura."
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
                    onAbrirActividad(actividad.id)
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
                        text = actividad.instrucciones
                    )

                    Text(
                        text = if (actividad.resuelta) {
                            "Realizada · Ver resultado"
                        } else {
                            "Pendiente · Resolver actividad"
                        },
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
