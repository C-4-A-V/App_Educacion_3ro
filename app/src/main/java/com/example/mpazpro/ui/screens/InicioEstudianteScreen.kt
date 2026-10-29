package com.example.mpazpro.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.model.PerfilEstudiante
import com.example.mpazpro.model.ProgresoEstudiante
import com.example.mpazpro.ui.components.ImagenInteligente

@Composable
fun InicioEstudianteScreen(
    estudiante: PerfilEstudiante,
    progreso: ProgresoEstudiante,
    onSalir: () -> Unit,
    onAbrirAsignatura: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onSalir)

    val asignaturas = listOf(
        "Lenguaje y Comunicación",
        "Matemática",
        "Ciencias Naturales",
        "Historia, Geografía y Ciencias Sociales"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "MPAZ PRO 3.º",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            ImagenInteligente(
                ruta = estudiante.fotoRuta,
                descripcion = "Tu fotografía de perfil"
            )
        }

        item {
            Text(
                text = "¡Hola, ${estudiante.nombre}!",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = estudiante.cursoNombre,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Tarjeta de Progreso General
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tu Progreso General",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    LinearProgressIndicator(
                        progress = { progreso.porcentaje },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${progreso.porcentajeTexto} completado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "${progreso.completadas} de ${progreso.total} actividades",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (progreso.completadas > 0) {
                        Text(
                            text = "Precisión de aciertos: ${progreso.porcentajeAciertosTexto} (${progreso.correctas} correctas de ${progreso.completadas})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Mis asignaturas",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Elige una asignatura para ver tus actividades.",
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
                            text = "Ver actividades",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onSalir,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salir de mi perfil")
            }
        }
    }
}
