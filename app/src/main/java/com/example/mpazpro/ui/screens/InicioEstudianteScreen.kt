package com.example.mpazpro.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import com.example.mpazpro.ui.components.FondoAventura
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import com.example.mpazpro.ui.components.ImagenInteligente
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
    mensaje: String? = null,
    paraContinuar: ActividadParaContinuar? = null,
    onContinuar: (ActividadParaContinuar) -> Unit = {},
    onAbrirPerfil: () -> Unit = {}
) {
    BackHandler(onBack = onSalir)
    val columnas = columnasPara(claseAnchoVentana())
    val avance by animateFloatAsState(progreso.porcentaje, label = "Progreso de la aventura")
    AventuraTheme {
        FondoAventura(modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnas),
                state = rememberLazyGridState(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("🌈 MI MUNDO DE AVENTURAS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                            Text("¡Hola, ${estudiante.nombre}!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Surface(
                            onClick = onAbrirPerfil,
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .25f))
                        ) {
                            Column(
                                Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ImagenInteligente(estudiante.fotoRuta, "Abrir mi perfil", Modifier.size(48.dp))
                                Text("Mi perfil", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                if (mensaje != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) { Text(mensaje, color = MaterialTheme.colorScheme.error) }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
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
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("⭐ ${progreso.correctas} estrellas · ¡Sigue brillando!", style = MaterialTheme.typography.titleMedium)
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
                if (paraContinuar != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            onClick = { onContinuar(paraContinuar) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Continuar mi aventura →", style = MaterialTheme.typography.titleLarge)
                                Text(Asignatura.buscarPorId(paraContinuar.asignaturaId)?.titulo.orEmpty(), style = MaterialTheme.typography.labelLarge)
                                Text(paraContinuar.titulo, style = MaterialTheme.typography.bodyLarge)
                                Text("Tu avance está guardado. ¡Sigamos aprendiendo!")
                            }
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
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(2.dp, asignatura.textoAventura().copy(alpha = .12f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = asignatura.colorAventura(), contentColor = asignatura.textoAventura())
                    ) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = CircleShape, color = androidx.compose.ui.graphics.Color.White.copy(alpha = .40f)) {
                                Text(asignatura.iconoAventura, Modifier.padding(12.dp), style = MaterialTheme.typography.headlineMedium)
                            }
                            Text(asignatura.titulo, style = MaterialTheme.typography.titleLarge)
                            Text(asignatura.descripcion, style = MaterialTheme.typography.bodyLarge)
                            Text("¡Vamos a explorar! →", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.labelLarge)
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
