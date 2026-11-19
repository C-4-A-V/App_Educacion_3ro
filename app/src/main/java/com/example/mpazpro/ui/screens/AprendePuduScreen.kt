package com.example.mpazpro.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.data.repository.TemaPudu
import com.example.mpazpro.ui.components.AnimoPudu
import com.example.mpazpro.ui.components.BotonVolver
import com.example.mpazpro.ui.components.FondoAventura
import com.example.mpazpro.ui.components.PuduExplorador
import com.example.mpazpro.ui.components.recordarLectorPudu
import com.example.mpazpro.ui.theme.AventuraTheme
import com.example.mpazpro.ui.viewmodel.AprendePuduViewModel

@Composable
fun AprendePuduScreen(onVolver: () -> Unit, viewModel: AprendePuduViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val navegador = LocalUriHandler.current
    val lector = recordarLectorPudu(estado.dato?.id, estado.tema, estado.cargando)
    AventuraTheme {
        FondoAventura(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BotonVolver(onClick = onVolver) { Text("Volver a mi inicio") }
                Text("✨ Aprende con Pudú", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("¡Elige un tema y descubramos algo nuevo!", style = MaterialTheme.typography.bodyLarge)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TemaPudu.entries.forEach { tema ->
                        FilterChip(
                            selected = estado.tema == tema,
                            onClick = { viewModel.elegirTema(tema) },
                            label = { Text(tema.etiqueta) }
                        )
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PuduExplorador(Modifier.size(110.dp), AnimoPudu.Alegre)
                        Text("¡Tengo algo para contarte!", style = MaterialTheme.typography.titleLarge)
                        if (estado.cargando) {
                            CircularProgressIndicator()
                            Text("Pudú está buscando un dato...")
                        } else {
                            estado.dato?.let { dato ->
                                Text(dato.texto, style = MaterialTheme.typography.bodyLarge)
                                OutlinedButton(
                                    onClick = { if (lector.leyendo) lector.detener() else lector.leer(dato.texto) },
                                    enabled = lector.listo,
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) { Text(if (lector.leyendo) "⏹ Detener lectura" else "🔊 Escuchar dato") }
                                lector.error?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                TextButton(onClick = { navegador.openUri(dato.enlace) }) {
                                    Text("Fuente: ${dato.fuente}")
                                }
                            }
                            if (estado.usandoDatoLocal) {
                                Text("Hoy usaremos un dato guardado en la app.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Button(
                    onClick = { viewModel.siguiente() },
                    enabled = !estado.cargando,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) { Text("¡Quiero aprender otro! ✨") }
            }
        }
    }
}
