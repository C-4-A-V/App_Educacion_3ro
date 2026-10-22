package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.ui.components.ImagenInteligente
import com.example.mpazpro.ui.components.SelectorFotoEstudiante
import com.example.mpazpro.ui.viewmodel.EstudiantesViewModel

@Composable
fun EstudiantesScreen(
    cursoId: Long,
    docenteId: Long,
    onVolver: () -> Unit,
    onGestionarActividades: () -> Unit,
    modifier: Modifier = Modifier,
    estudiantesViewModel: EstudiantesViewModel = viewModel()
) {
    val estado by estudiantesViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cursoId, docenteId) {
        estudiantesViewModel.cargar(cursoId, docenteId)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TextButton(onClick = onVolver) {
                Text("Volver a mis cursos")
            }
        }

        item {
            Text(
                text = estado.curso?.nombre ?: "Curso",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (estado.cargando || estado.guardando) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        estado.mensaje?.let { mensaje ->
            item { Text(mensaje) }
        }

        if (estado.curso != null && !estado.cargando) {
            item {
                OutlinedButton(
                    onClick = onGestionarActividades,
                    enabled = !estado.guardando && !estado.importandoFoto,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gestionar actividades")
                }
            }

            item {
                Text(
                    text = "Registrar estudiante",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Text("Usa nombres ficticios para las pruebas.")
            }

            item {
                SelectorFotoEstudiante(
                    fotoRuta = estado.fotoRuta,
                    errorFoto = estado.errorFoto,
                    habilitado = !estado.guardando && !estado.importandoFoto,
                    onSeleccion = estudiantesViewModel::seleccionarFoto,
                    onError = estudiantesViewModel::mostrarErrorFoto
                )
            }

            if (estado.importandoFoto) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Preparando fotografía…")
                }
            }

            item {
                OutlinedTextField(
                    value = estado.nombre,
                    onValueChange = estudiantesViewModel::actualizarNombre,
                    label = { Text("Nombre del estudiante") },
                    singleLine = true,
                    enabled = !estado.guardando && !estado.importandoFoto,
                    isError = estado.errorNombre != null,
                    supportingText = {
                        estado.errorNombre?.let { Text(it) }
                    },
                    trailingIcon = {
                        if (estado.errorNombre != null) {
                            Icon(
                                painter = painterResource(
                                    android.R.drawable.ic_dialog_alert
                                ),
                                contentDescription = "Error en el nombre",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = estudiantesViewModel::registrar,
                    enabled = !estado.guardando && !estado.importandoFoto,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (estado.guardando) "Guardando…"
                        else "Registrar estudiante"
                    )
                }
            }

            item {
                Text(
                    text = "Estudiantes: ${estado.estudiantes.size}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (estado.estudiantes.isEmpty()) {
                item {
                    Text("Todavía no hay estudiantes en este curso.")
                }
            }

            items(
                items = estado.estudiantes,
                key = { it.id }
            ) { estudiante ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ImagenInteligente(
                            ruta = estudiante.fotoRuta,
                            descripcion = "Fotografía de ${estudiante.nombre}"
                        )
                        Text(
                            text = estudiante.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Identificador de prueba: E-${estudiante.id}")
                    }
                }
            }
        }
    }
}
