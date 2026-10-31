package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.model.ProgresoEstudiante
import com.example.mpazpro.ui.components.ImagenInteligente
import com.example.mpazpro.ui.viewmodel.ResumenDocenteViewModel

@Composable
fun ResumenDocenteScreen(
    docenteId: Long,
    cursoId: Long = 0L,
    estudianteId: Long = 0L,
    onVolver: () -> Unit,
    onAbrirCurso: (Long) -> Unit = {},
    onAbrirEstudiante: (Long) -> Unit = {},
    resumenViewModel: ResumenDocenteViewModel = viewModel()
) {
    val estado by resumenViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(docenteId) {
        resumenViewModel.cargar(docenteId)
    }

    val curso = estado.cursos.find {
        it.id == cursoId
    }

    val alumnos = estado.alumnos.filter {
        it.cursoId == cursoId
    }

    val estudiante = alumnos.find {
        it.id == estudianteId
    }

    LazyColumn(
        modifier = Modifier
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
                text = when {
                    estudianteId != 0L -> "Avance del estudiante"
                    cursoId != 0L -> "Resultados · ${curso?.nombre ?: "Curso"}"
                    else -> "Resultados por curso"
                },
                style = MaterialTheme.typography.headlineMedium
            )
        }

        when {
            estado.cargando -> {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            estado.error != null -> {
                item {
                    Text(estado.error.orEmpty())

                    Button(
                        onClick = {
                            resumenViewModel.cargar(docenteId)
                        }
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            // Primera pantalla: cursos en globos.
            cursoId == 0L -> {
                if (estado.cursos.isEmpty()) {
                    item {
                        Text(
                            "Aún no tienes cursos. " +
                                "Puedes crear uno desde Mis cursos."
                        )
                    }
                }

                items(estado.cursos.chunked(2)) { fila ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        fila.forEach { actual ->
                            Card(
                                onClick = {
                                    onAbrirCurso(actual.id)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 130.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "📚",
                                        style = MaterialTheme.typography.headlineMedium
                                    )

                                    Text(
                                        text = actual.nombre,
                                        style = MaterialTheme.typography.titleLarge
                                    )

                                    Text(
                                        text = "${
                                            estado.alumnos.count {
                                                it.cursoId == actual.id
                                            }
                                        } estudiantes"
                                    )
                                }
                            }
                        }

                        if (fila.size == 1) {
                            Spacer(
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            curso == null -> {
                item {
                    Text(
                        "Este curso no está disponible para tu cuenta."
                    )
                }
            }

            // Segunda pantalla: estudiantes del curso.
            estudianteId == 0L -> {
                if (alumnos.isEmpty()) {
                    item {
                        Text(
                            "Este curso todavía no tiene estudiantes."
                        )
                    }
                }

                items(
                    items = alumnos,
                    key = { it.id }
                ) { alumno ->
                    Card(
                        onClick = {
                            onAbrirEstudiante(alumno.id)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ImagenInteligente(
                                    ruta = alumno.fotoRuta,
                                    descripcion = "Foto de ${alumno.nombre}",
                                    modifier = Modifier.size(52.dp)
                                )

                                Text(
                                    text = alumno.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            AvanceDocente(alumno.general)

                            Text("Ver las cuatro asignaturas →")
                        }
                    }
                }
            }

            estudiante == null -> {
                item {
                    Text(
                        "Estudiante no disponible en este curso."
                    )
                }
            }

            // Tercera pantalla: detalle de las cuatro asignaturas.
            else -> {
                item {
                    ImagenInteligente(
                        ruta = estudiante.fotoRuta,
                        descripcion = "Foto de ${estudiante.nombre}"
                    )

                    Text(
                        text = estudiante.nombre,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(curso.nombre)
                }

                item {
                    Text(
                        text = "Avance general",
                        style = MaterialTheme.typography.titleMedium
                    )

                    AvanceDocente(estudiante.general)
                }

                items(
                    items = Asignatura.entries.toList(),
                    key = { it.id }
                ) { materia ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = materia.titulo,
                                style = MaterialTheme.typography.titleMedium
                            )

                            AvanceDocente(
                                progreso = estudiante.materias[materia.id]
                                    ?: ProgresoEstudiante()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvanceDocente(
    progreso: ProgresoEstudiante
) {
    if (progreso.total == 0) {
        Text("Sin actividades asignadas")
    } else {
        LinearProgressIndicator(
            progress = { progreso.porcentaje },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "${progreso.porcentajeTexto} · " +
                "${progreso.completadas} de ${progreso.total} " +
                "actividades completadas"
        )

        Text(
            text = "Aciertos: ${progreso.porcentajeAciertosTexto}"
        )
    }

    Text(
        text = "⭐ ${progreso.correctas} estrellas"
    )
}
