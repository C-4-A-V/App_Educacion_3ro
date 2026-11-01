package com.example.mpazpro.ui.screens

import com.example.mpazpro.ui.theme.DocenteTheme
import com.example.mpazpro.ui.theme.EncabezadoDocente

import com.example.mpazpro.ui.components.BotonVolver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.mpazpro.ui.components.anchoFormulario
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
    DocenteTheme {
    val estado by perfilViewModel.uiState.collectAsStateWithLifecycle()
    val lectura by perfilViewModel.lectura.collectAsStateWithLifecycle()
    val modoOscuro = estado.modoOscuro ?: isSystemInDarkTheme()

    LaunchedEffect(docenteId) {
        perfilViewModel.cargar(docenteId)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize().anchoFormulario()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BotonVolver(onClick = onVolver) {
                Text("Volver al menú")
            }
        }

        item {
            EncabezadoDocente(
                titulo = "Mi perfil",
                detalle = "Tu información y preferencias para trabajar con comodidad."
            )
        }

        if (estado.cargando) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Modo oscuro", style = MaterialTheme.typography.titleMedium)
                        Text("Una apariencia más suave para trabajar con poca luz.")
                        Text(
                            "Se guarda para tu cuenta docente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = modoOscuro,
                        onCheckedChange = perfilViewModel::cambiarModoOscuro,
                        enabled = !estado.cargando && !estado.guardando && estado.docente?.id == docenteId,
                        modifier = Modifier.semantics { contentDescription = "Modo oscuro" }
                    )
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Lectura cómoda", style = MaterialTheme.typography.titleMedium)
                    Text("Aumenta el texto de la app. Se conserva al volver a abrirla.")
                }
                Switch(
                    checked = lectura,
                    onCheckedChange = perfilViewModel::cambiarLectura,
                    enabled = !estado.guardando
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
                Text("Guardando cambios…")
            }
        }

        estado.mensaje?.let { mensaje ->
            item(key = "mensaje_$mensaje") {
                com.example.mpazpro.ui.components.FeedbackAnimado { Text(mensaje) }
            }
        }
    }
    }
}
