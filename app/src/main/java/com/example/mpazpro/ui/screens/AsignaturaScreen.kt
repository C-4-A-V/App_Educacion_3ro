package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
                Text("← Volver a mis asignaturas")
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
                text = asignatura.descripcion,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Tarjeta de Progreso de la Asignatura
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Progreso en ${asignatura.titulo}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    LinearProgressIndicator(
                        progress = { estado.progreso.porcentaje },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${estado.progreso.porcentajeTexto} completado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Text(
                            text = "${estado.progreso.completadas} de ${estado.progreso.total} actividades",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    if (estado.progreso.completadas > 0) {
                        Text(
                            text = "Aciertos: ${estado.progreso.porcentajeAciertosTexto} (${estado.progreso.correctas} correctas)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Actividades disponibles",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
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

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (actividad.resuelta) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    ) {
                        Text(
                            text = if (actividad.resuelta) "✅ Realizada · Ver resultado" else "⭐ Pendiente · Resolver",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
