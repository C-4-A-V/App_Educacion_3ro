package com.example.mpazpro.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mpazpro.model.*
import com.example.mpazpro.ui.components.PuduExplorador
import com.example.mpazpro.ui.theme.*
import com.example.mpazpro.ui.utils.claseAnchoVentana
import com.example.mpazpro.ui.utils.columnasPara

@Composable
fun InicioEstudianteScreen(
    estudiante: PerfilEstudiante,
    progreso: ProgresoEstudiante,
    onSalir: () -> Unit,
    onAbrirAsignatura: (String) -> Unit,
    modifier: Modifier = Modifier,
    mensaje: String? = null
) {
    BackHandler(onBack = onSalir)
    val columnas = columnasPara(claseAnchoVentana())
    val avance by animateFloatAsState(progreso.porcentaje, label = "Progreso de la aventura")
    AventuraTheme {
        Surface(modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnas),
                state = rememberLazyGridState(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text("MPAZ PRO · 3.º básico", style = MaterialTheme.typography.labelLarge)
                        Text("¡Hola, ${estudiante.nombre}!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (mensaje != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) { Text(mensaje, color = MaterialTheme.colorScheme.error) }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PuduExplorador(Modifier.size(76.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("¿Qué descubriremos hoy?", style = MaterialTheme.typography.titleMedium)
                                Text("¡Vamos a aprender con tu pudú explorador!", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(shape = RoundedCornerShape(24.dp)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("⭐ ${progreso.correctas} estrellas · Tu aventura", style = MaterialTheme.typography.titleMedium)
                            LinearProgressIndicator(progress = { avance }, modifier = Modifier.fillMaxWidth().height(10.dp))
                            Text("${progreso.completadas} de ${progreso.total} actividades completadas")
                            Text(
                                when {
                                    progreso.total == 0 -> "Tu docente pronto preparará nuevas aventuras."
                                    progreso.completadas == progreso.total -> "¡Completaste todas tus actividades! Gran trabajo."
                                    progreso.completadas > 0 -> "¡Cada actividad que terminas es un nuevo paso!"
                                    else -> "Elige una asignatura y comienza a explorar."
                                }, style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("Elige tu próxima aventura", style = MaterialTheme.typography.titleLarge)
                }
                items(Asignatura.entries, key = { it.id }) { asignatura ->
                    Card(
                        onClick = { onAbrirAsignatura(asignatura.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = asignatura.colorAventura(), contentColor = asignatura.textoAventura())
                    ) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(asignatura.iconoAventura, style = MaterialTheme.typography.headlineMedium)
                            Text(asignatura.titulo, style = MaterialTheme.typography.titleLarge)
                            Text(asignatura.descripcion, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedButton(onSalir, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Salir de mi perfil") }
                }
            }
        }
    }
}
