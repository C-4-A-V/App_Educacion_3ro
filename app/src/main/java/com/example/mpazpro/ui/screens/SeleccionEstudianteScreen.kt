package com.example.mpazpro.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.ui.theme.AventuraTheme
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
            TextButton(onClick = onVolver) {
                Text("Volver al inicio")
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "¿Quién va a aprender? 🎒",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Selecciona tu perfil para comenzar.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (estado.cargando) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        estado.mensaje?.let { mensaje ->
            item(span = { GridItemSpan(maxLineSpan) }) { Text(mensaje) }
        }

        if (
            !estado.cargando &&
            estado.mensaje == null &&
            estado.perfiles.isEmpty()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Todavía no hay estudiantes registrados. Un docente debe agregarlos desde su panel.",
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }

        items(
            items = estado.perfiles,
            key = { it.id }
        ) { perfil ->
            Card(
                onClick = { onSeleccionar(perfil.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
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
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = perfil.cursoNombre,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "Toca para comenzar →",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    }
    }
}
