package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.ui.components.ImagenInteligente
import com.example.mpazpro.ui.viewmodel.AccesoEstudianteViewModel

@Composable
fun SeleccionEstudianteScreen(
    accesoViewModel: AccesoEstudianteViewModel,
    onSeleccionar: (Long) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by accesoViewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TextButton(onClick = onVolver) {
                Text("Volver al inicio")
            }
        }

        item {
            Text(
                text = "¿Quién va a aprender?",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text("Selecciona tu perfil de prueba.")
        }

        if (estado.cargando) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        estado.mensaje?.let { mensaje ->
            item { Text(mensaje) }
        }

        if (
            !estado.cargando &&
            estado.mensaje == null &&
            estado.perfiles.isEmpty()
        ) {
            item {
                Text(
                    "Todavía no hay estudiantes. Un docente debe " +
                        "registrarlos desde su curso."
                )
            }
        }

        items(
            items = estado.perfiles,
            key = { it.id }
        ) { perfil ->
            Card(
                onClick = { onSeleccionar(perfil.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ImagenInteligente(
                        ruta = perfil.fotoRuta,
                        descripcion = "Fotografía de ${perfil.nombre}"
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = perfil.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(perfil.cursoNombre)

                        Text(
                            text = "E-${perfil.id}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
