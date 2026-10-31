package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.ui.components.SelectorFotoEstudiante
import com.example.mpazpro.ui.viewmodel.PerfilDocenteViewModel

@Composable
fun PerfilDocenteScreen(
    docenteId: Long,
    perfilViewModel: PerfilDocenteViewModel,
    onVolver: () -> Unit
) {
    val estado by perfilViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(docenteId) {
        perfilViewModel.cargar(docenteId)
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
                Text("Volver al menú")
            }
        }

        item {
            Text(
                text = "Mi perfil",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (estado.cargando) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        estado.docente?.let { docente ->
            item {
                Text(
                    text = "Nombre",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = docente.nombre,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Text(
                    text = "Correo electrónico",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(docente.correo)
            }

            item {
                Text("Fotografía del docente")
            }

            item {
                SelectorFotoEstudiante(
                    fotoRuta = docente.fotoRuta,
                    errorFoto = null,
                    habilitado = !estado.guardando,
                    onSeleccion = perfilViewModel::guardarFoto,
                    onError = perfilViewModel::mostrarError
                )
            }
        }

        if (estado.guardando) {
            item {
                Text("Guardando fotografía…")
            }
        }

        estado.mensaje?.let { mensaje ->
            item {
                Text(mensaje)
            }
        }
    }
}
