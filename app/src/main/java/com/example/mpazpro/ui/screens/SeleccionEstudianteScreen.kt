package com.example.mpazpro.ui.screens

import com.example.mpazpro.ui.components.BotonVolver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.model.AccesoEstudianteUiState
import com.example.mpazpro.ui.components.FondoAventura
import com.example.mpazpro.ui.components.ImagenInteligente
import com.example.mpazpro.ui.theme.AventuraTheme
import com.example.mpazpro.ui.theme.colorTarjetaAventura
import com.example.mpazpro.ui.utils.claseAnchoVentana
import com.example.mpazpro.ui.utils.columnasPara
import com.example.mpazpro.ui.viewmodel.AccesoEstudianteViewModel

@Composable
fun SeleccionEstudianteScreen(
    accesoViewModel: AccesoEstudianteViewModel,
    onSeleccionar: (Long) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by accesoViewModel.uiState.collectAsStateWithLifecycle()
    SeleccionEstudianteContenido(estado, onSeleccionar, onVolver, modifier)
}

@Composable
internal fun SeleccionEstudianteContenido(
    estado: AccesoEstudianteUiState,
    onSeleccionar: (Long) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    AventuraTheme {
        FondoAventura(modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnasPara(claseAnchoVentana())),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(24.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row { BotonVolver(onClick = onVolver) { Text("Volver al inicio") } }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text("🎒 TU PRÓXIMA AVENTURA", Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                        Text("¿Quién va a aprender?", style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary)
                        Text("Busca tu nombre. ¡Hoy descubrirás algo nuevo!",
                            style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (estado.cargando) {
                    item(span = { GridItemSpan(maxLineSpan) }) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                }
                estado.mensaje?.let { mensaje ->
                    item(span = { GridItemSpan(maxLineSpan) }) { Text(mensaje, color = MaterialTheme.colorScheme.error) }
                }
                if (!estado.cargando && estado.mensaje == null && estado.perfiles.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(shape = RoundedCornerShape(24.dp)) {
                            Text("Tu aventura está por comenzar. Pide a tu docente que agregue tu perfil.",
                                Modifier.padding(24.dp), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                items(estado.perfiles, key = { it.id }) { perfil ->
                    val indice = (perfil.id % 4).toInt()
                    val (fondo, texto) = colorTarjetaAventura(indice)
                    Card(
                        onClick = { onSeleccionar(perfil.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = texto),
                        border = BorderStroke(2.dp, texto.copy(alpha = .12f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(24.dp), color = Color.White.copy(alpha = .55f)) {
                                Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                                    if (perfil.fotoRuta.isNullOrBlank()) {
                                        Text(listOf("🦊", "🐼", "🐸", "🐨")[indice],
                                            style = MaterialTheme.typography.displaySmall)
                                    } else {
                                        ImagenInteligente(perfil.fotoRuta, "Fotografía de ${perfil.nombre}", Modifier.size(72.dp))
                                    }
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(perfil.nombre, style = MaterialTheme.typography.titleLarge)
                                Text(perfil.cursoNombre, style = MaterialTheme.typography.bodyMedium)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = texto.copy(alpha = .10f), contentColor = texto
                                ) {
                                    Text("¡Vamos! →", Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("🌟 Cada explorador aprende a su propio ritmo.",
                        modifier = Modifier.padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
