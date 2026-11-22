package com.example.mpazpro.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.luminance
import com.example.mpazpro.data.repository.PreferenciasPerfilEstudiante
import com.example.mpazpro.data.repository.PaletaEstudiante
import com.example.mpazpro.model.AccesoEstudianteUiState
import com.example.mpazpro.ui.components.*
import com.example.mpazpro.ui.theme.AventuraTheme
import android.net.Uri

@Composable
fun PerfilEstudianteScreen(
    estado: AccesoEstudianteUiState,
    onVolver: () -> Unit,
    onContinuar: () -> Unit,
    onOscuro: (Boolean) -> Unit,
    onPaleta: (PaletaEstudiante) -> Unit = {},
    onTextoGrande: (Boolean) -> Unit,
    onObjetivo: (String) -> Unit,
    onFoto: (Uri) -> Unit,
    onErrorFoto: (String) -> Unit
) {
    val estudiante = estado.seleccionado ?: return
    var objetivoBorrador by rememberSaveable(estudiante.id, estado.preferencias.objetivo) {
        mutableStateOf(estado.preferencias.objetivo)
    }
    val foco = LocalFocusManager.current
    val editable = estado.preferenciasCargadas && !estado.guardandoPerfil
    val avance by animateFloatAsState(estado.progreso.porcentaje, label = "Mi progreso")
    AventuraTheme {
        FondoAventura(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().anchoFormulario().imePadding(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    BotonVolver(onClick = onVolver) { Text("Volver a mis aventuras") }
                }
                item {
                    Text("Mi perfil", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    Text("¡Este es tu espacio para seguir creciendo!")
                }
                item {
                    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                ImagenInteligente(estudiante.fotoRuta, "Mi fotografía", Modifier.size(88.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(estudiante.nombre, style = MaterialTheme.typography.titleLarge)
                                    Text(com.example.mpazpro.data.repository.PropuestaVinculacionCursos.nombreVisible(estudiante.cursoNombre), style = MaterialTheme.typography.bodyLarge)
                                    Text("Estudiante MPAZ", color = MaterialTheme.colorScheme.secondary,
                                        style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Text("Mi fotografía", style = MaterialTheme.typography.titleMedium)
                            key(estudiante.id) {
                                SelectorFotoEstudiante(
                                    fotoRuta = estudiante.fotoRuta, errorFoto = null,
                                    habilitado = editable, onSeleccion = onFoto, onError = onErrorFoto,
                                    mostrarVistaPrevia = false
                                )
                            }
                        }
                    }
                }
                item {
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("🎯 Mi objetivo", style = MaterialTheme.typography.titleLarge)
                            Text("¿Qué te gustaría aprender o mejorar?")
                            OutlinedTextField(
                                value = objetivoBorrador,
                                onValueChange = { if (it.length <= PreferenciasPerfilEstudiante.MAX_OBJETIVO) objetivoBorrador = it },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    disabledContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                label = { Text("Escribe tu objetivo") },
                                placeholder = { Text("Por ejemplo: leer un cuento cada semana") },
                                supportingText = { Text("${objetivoBorrador.length}/${PreferenciasPerfilEstudiante.MAX_OBJETIVO} caracteres") },
                                minLines = 2, maxLines = 4,
                                enabled = editable,
                                modifier = Modifier.fillMaxWidth()
                            )
                            BotonSecundario(
                                onClick = {
                                    foco.clearFocus()
                                    onObjetivo(objetivoBorrador.trim())
                                },
                                enabled = editable && objetivoBorrador.isNotBlank() &&
                                    objetivoBorrador.trim() != estado.preferencias.objetivo
                            ) {
                                Text("Guardar mi objetivo")
                            }
                        }
                    }
                }
                item {
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Mi progreso de aprendizaje", style = MaterialTheme.typography.titleLarge)
                            Text("${estado.progreso.porcentajeTexto} de mi aventura completada")
                            LinearProgressIndicator(progress = { avance }, modifier = Modifier.fillMaxWidth().height(12.dp))
                            Text("${estado.progreso.completadas} de ${estado.progreso.total} actividades terminadas")
                            Text("⭐ ${estado.progreso.correctas} estrellas por mis aciertos")
                        }
                    }
                }
                item {
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("A mi manera", style = MaterialTheme.typography.titleLarge)
                            Text("🎨 Elige tus colores", style = MaterialTheme.typography.titleMedium)
                            PaletaEstudiante.entries.forEach { paleta ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().selectable(
                                        selected = estado.preferencias.paleta == paleta,
                                        enabled = editable,
                                        role = Role.RadioButton,
                                        onClick = { onPaleta(paleta) }
                                    ).heightIn(min = 48.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = estado.preferencias.paleta == paleta,
                                        onClick = null,
                                        enabled = editable
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(paleta.titulo, style = MaterialTheme.typography.titleMedium)
                                        Text(paleta.detalle, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text(when (paleta) {
                                        PaletaEstudiante.AVENTURA -> "💜🩷"
                                        PaletaEstudiante.OCEANO -> "💙💚"
                                        PaletaEstudiante.ATARDECER -> "❤️💛"
                                    }, style = MaterialTheme.typography.titleLarge)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            AjustePerfilEstudiante(
                                "Modo oscuro", "Elige cómo se ve tu aventura.",
                                estado.preferencias.modoOscuro ?: (MaterialTheme.colorScheme.background.luminance() < .5f),
                                editable, onOscuro
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            AjustePerfilEstudiante(
                                "Texto más grande", "Un poquito de ayuda para leer con comodidad.",
                                estado.preferencias.textoGrande, editable, onTextoGrande
                            )
                            Text("Tus ajustes se guardan solo para tu perfil en este dispositivo.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (estado.guardandoPerfil || !estado.preferenciasCargadas) {
                    item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                }
                estado.mensajePerfil?.let { mensaje ->
                    item { Text(mensaje, style = MaterialTheme.typography.bodyLarge) }
                }
                item {
                    Button(
                        onClick = onContinuar,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) { Text("Continuar mi aventura →") }
                }
            }
        }
    }
}

@Composable
private fun AjustePerfilEstudiante(
    titulo: String, detalle: String, activado: Boolean, habilitado: Boolean,
    onCambiar: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(detalle, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = activado, onCheckedChange = onCambiar, enabled = habilitado,
            modifier = Modifier.semantics { contentDescription = titulo })
    }
}
