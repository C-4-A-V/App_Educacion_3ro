package com.example.mpazpro.ui.screens

import com.example.mpazpro.ui.components.BotonVolver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import com.example.mpazpro.ui.utils.claseAnchoVentana
import com.example.mpazpro.ui.utils.columnasPara
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.ui.theme.*
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
    actividadesViewModel: ActividadesEstudianteViewModel = viewModel(),
    onAbrirActividadRemota: (Long) -> Unit = {}
) {
    val estado by actividadesViewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(estudiante.id, asignatura.id) {
        actividadesViewModel.cargar(
            estudianteId = estudiante.id,
            asignaturaId = asignatura.id
        )
    }

    ActualizacionAutomatica(estudiante.id to asignatura.id) {
        actividadesViewModel.actualizarCompartidasAutomaticamente()
    }

    AventuraTheme {
    Surface(modifier.fillMaxSize()) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columnasPara(claseAnchoVentana())),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            BotonVolver(onClick = onVolver) {
                Text("Volver a mis asignaturas")
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "${asignatura.iconoAventura} ${asignatura.titulo}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = asignatura.descripcion,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Tarjeta de Progreso de la Asignatura
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = asignatura.colorAventura(),
                    contentColor = asignatura.textoAventura()
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Avance anterior en ${asignatura.titulo}",
                        style = MaterialTheme.typography.titleMedium,
                        color = asignatura.textoAventura()
                    )

                    LinearProgressIndicator(
                        progress = { estado.progreso.porcentaje },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${estado.progreso.porcentajeTexto} completado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = asignatura.textoAventura()
                        )

                        Text(
                            text = "${estado.progreso.completadas} de ${estado.progreso.total} actividades",
                            style = MaterialTheme.typography.bodyMedium,
                            color = asignatura.textoAventura()
                        )
                    }

                    Text("Este avance corresponde a prácticas anteriores guardadas en este dispositivo; no incluye preguntas compartidas.", style = MaterialTheme.typography.bodySmall)

                    if (estado.progreso.completadas > 0) {
                        Text(
                            text = "Aciertos: ${estado.progreso.porcentajeAciertosTexto} (${estado.progreso.correctas} correctas)",
                            style = MaterialTheme.typography.bodySmall,
                            color = asignatura.textoAventura()
                        )
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Actividades disponibles",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (estado.cargando) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        estado.mensaje?.let { mensaje ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (
            !estado.cargando &&
            estado.mensaje == null &&
            estado.actividades.isEmpty() &&
            estado.actividadesCompartidas.isEmpty() &&
            !estado.cargandoCompartidas
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
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

        if (estado.cargandoCompartidas) {
            item(span = { GridItemSpan(maxLineSpan) }) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        }
        if (estado.errorCompartidas) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Text("No pudimos cargar las preguntas compartidas. Comprueba la conexión.")
                    TextButton(onClick = actividadesViewModel::actualizarCompartidas) { Text("Intentar otra vez") }
                }
            }
        }
        if (estado.actividadesCompartidas.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text("Preguntas compartidas", style = MaterialTheme.typography.titleMedium)
            }
            items(estado.actividadesCompartidas, key = { "remota_${it.id}" }) { actividad ->
                Card(
                    onClick = { onAbrirActividadRemota(actividad.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = asignatura.colorAventura(),
                        contentColor = asignatura.textoAventura()
                    )
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(actividad.titulo, style = MaterialTheme.typography.titleMedium)
                        Text(actividad.instrucciones)
                        Text("Pregunta compartida · Abrir", style = MaterialTheme.typography.labelMedium)
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
                    containerColor = asignatura.colorAventura(),
                        contentColor = asignatura.textoAventura()
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
                            text = when {
                                actividad.resuelta -> "✓ Completada · Ver resultado"
                                actividad.intentosUsados > 0 -> "Continuar · ${actividad.maxIntentos - actividad.intentosUsados} oportunidades"
                                actividad.maxIntentos == 3 -> "Práctica · Comenzar"
                                else -> "Evaluación · Comenzar"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (actividad.resuelta) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
    }
    }
}
