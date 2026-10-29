package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.ui.viewmodel.CursoDocenteViewModel

@Composable
fun AsignaturasDocenteScreen(
    cursoId: Long,
    docenteId: Long,
    onVolver: () -> Unit,
    onAbrirAsignatura: (String) -> Unit,
    modifier: Modifier = Modifier,
    cursoViewModel: CursoDocenteViewModel = viewModel()
) {
    val estado by cursoViewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(cursoId, docenteId) {
        cursoViewModel.cargar(
            cursoId = cursoId,
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
                text = "Gestionar actividades",
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

        if (curso != null && !estado.cargando) {
            item {
                Text(
                    text = curso.nombre,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Elige la asignatura que deseas gestionar.",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Asignatura.entries.forEach { asignatura ->
                item(key = asignatura.id) {
                    Card(
                        onClick = {
                            onAbrirAsignatura(asignatura.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = asignatura.titulo,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = asignatura.descripcion,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Gestionar esta asignatura",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
