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
import com.example.mpazpro.model.DocenteSesion
import com.example.mpazpro.ui.viewmodel.CursosViewModel

@Composable
fun PanelDocenteScreen(
    docente: DocenteSesion,
    onCerrarSesion: () -> Unit,
    onAbrirCurso: (Long) -> Unit,
    modifier: Modifier = Modifier,
    cursosViewModel: CursosViewModel = viewModel()
) {
    val estado by cursosViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(docente.id) {
        cursosViewModel.cargarCursos(docente.id)
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
            Text(
                text = "Panel docente",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text(
                text = "Hola, ${docente.nombre}",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Text(
                text = "Mis cursos de Tercero Básico",
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            OutlinedTextField(
                value = estado.nombre,
                onValueChange = cursosViewModel::actualizarNombre,
                label = { Text("Nombre del curso") },
                placeholder = { Text("Ejemplo: 3.º A") },
                singleLine = true,
                enabled = !estado.guardando && !estado.cargando,
                isError = estado.errorNombre != null,
                supportingText = {
                    estado.errorNombre?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = cursosViewModel::crearCurso,
                enabled = !estado.guardando && !estado.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (estado.guardando) "Guardando…" else "Crear curso"
                )
            }
        }

        if (estado.cargando || estado.guardando) {
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
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (!estado.cargando && estado.cursos.isEmpty()) {
            item {
                Text("Todavía no tienes cursos registrados.")
            }
        }

        items(
            items = estado.cursos,
            key = { it.id }
        ) { curso ->
            Card(
                onClick = { onAbrirCurso(curso.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = curso.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Toca para ver estudiantes")
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onCerrarSesion,
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
