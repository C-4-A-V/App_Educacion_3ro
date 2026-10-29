package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.ui.components.ImagenInteligente
import com.example.mpazpro.ui.viewmodel.ResultadosActividadViewModel

@Composable
fun ResultadosActividadScreen(
    actividadId: Long,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    resultadosViewModel: ResultadosActividadViewModel = viewModel()
) {
    val estado by resultadosViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(actividadId) {
        resultadosViewModel.cargar(actividadId)
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
                Text("Volver a las actividades")
            }
        }

        val actividad = estado.actividad

        item {
            Text(
                text = actividad?.titulo ?: "Resultados de la actividad",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        actividad?.let {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pregunta:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = it.pregunta,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Tarjeta de Resumen Estadístico para el Docente
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Resumen del Curso",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    Text(
                        text = "Respondidas: ${estado.totalRespondidas} de ${estado.totalEstudiantes} estudiantes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    Text(
                        text = "Aciertos del curso: ${estado.porcentajeAciertoTexto} (${estado.totalCorrectas} correctas · ${estado.totalIncorrectas} incorrectas)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
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

        item {
            Text(
                text = "Detalle por estudiante",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (!estado.cargando && estado.resultados.isEmpty()) {
            item {
                Text("No hay estudiantes registrados en este curso.")
            }
        }

        items(
            items = estado.resultados,
            key = { it.estudianteId }
        ) { resultado ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ImagenInteligente(
                        ruta = resultado.estudianteFoto,
                        descripcion = "Fotografía de ${resultado.estudianteNombre}"
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = resultado.estudianteNombre,
                            style = MaterialTheme.typography.titleMedium
                        )

                        val estadoTexto = when {
                            !resultado.respondida -> "Pendiente de resolver"
                            resultado.correcta == true -> "¡Correcta! (Alternativa ${"ABCD"[resultado.respuestaElegida ?: 0]})"
                            else -> "Incorrecta (Alternativa ${"ABCD"[resultado.respuestaElegida ?: 0]})"
                        }

                        val colorEstado = when {
                            !resultado.respondida -> MaterialTheme.colorScheme.outline
                            resultado.correcta == true -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        }

                        Text(
                            text = estadoTexto,
                            color = colorEstado,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
