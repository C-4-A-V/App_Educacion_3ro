package com.example.mpazpro.ui.screens

import com.example.mpazpro.ui.theme.DocenteTheme
import com.example.mpazpro.ui.theme.EncabezadoDocente

import com.example.mpazpro.ui.components.BotonVolver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.mpazpro.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.example.mpazpro.ui.components.anchoFormulario
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
    DocenteTheme {
    val estado by estudiantesViewModel.uiState.collectAsStateWithLifecycle()
    var editandoLista by remember(cursoId, docenteId) { mutableStateOf(false) }
    var seleccionados by remember(cursoId, docenteId) { mutableStateOf<Set<Long>>(emptySet()) }
    var confirmarRetiro by remember(cursoId, docenteId) { mutableStateOf(false) }
    var verRetirados by remember(cursoId, docenteId) { mutableStateOf(false) }

    if (confirmarRetiro) {
        AlertDialog(
            onDismissRequest = { confirmarRetiro = false },
            title = { Text("Retirar estudiantes") },
            text = { Text("Se retirarán ${seleccionados.size} estudiantes de este curso y no podrán ingresar mientras estén retirados. Sus perfiles, fotos y avances se conservan y podrás restaurarlos.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarRetiro = false
                    estudiantesViewModel.retirarEstudiantes(seleccionados) {
                        seleccionados = emptySet()
                        editandoLista = false
                    }
                }) { Text("Retirar") }
            },
            dismissButton = { TextButton(onClick = { confirmarRetiro = false }) { Text("Cancelar") } }
        )
    }

    if (estado.editandoAccesoId != null) {
        AlertDialog(
            onDismissRequest = { if (!estado.guardando) estudiantesViewModel.cancelarAcceso() },
            title = { Text("Configurar acceso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ingresa el RUT y un PIN nuevo de 4 números para este estudiante.")
                    CamposAccesoEstudiante(estado.rut, estado.pin, estado.errorAcceso,
                        estudiantesViewModel::actualizarRut, estudiantesViewModel::actualizarPin, !estado.guardando)
                }
            },
            confirmButton = { TextButton(onClick = estudiantesViewModel::guardarAcceso, enabled = !estado.guardando) { Text("Guardar") } },
            dismissButton = { TextButton(onClick = estudiantesViewModel::cancelarAcceso, enabled = !estado.guardando) { Text("Cancelar") } }
        )
    }

    LaunchedEffect(cursoId, docenteId) {
        estudiantesViewModel.cargar(cursoId, docenteId)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize().anchoFormulario()
            .imePadding()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BotonVolver(onClick = onVolver) {
                Text("Volver a mis cursos")
            }
        }

        item {
            EncabezadoDocente(
                titulo = estado.curso?.nombre?.let(com.example.mpazpro.data.repository.PropuestaVinculacionCursos::nombreVisible) ?: "Curso",
                detalle = "Organiza tu grupo y acompaña a cada estudiante."
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
            // Menú moderno en tarjetas / burbujas visuales
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = onGestionarActividades,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(painterResource(android.R.drawable.ic_menu_agenda), contentDescription = null)
                            Text(
                                text = "Actividades",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Crear y revisar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(painterResource(android.R.drawable.ic_menu_myplaces), contentDescription = null)
                            Text(
                                text = "Estudiantes",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${estado.estudiantes.size} registrados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Registrar nuevo estudiante",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Text("Usa nombres ficticios para las pruebas de demostración.")
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
                CamposAccesoEstudiante(estado.rut, estado.pin, estado.errorAcceso,
                    estudiantesViewModel::actualizarRut, estudiantesViewModel::actualizarPin,
                    !estado.guardando && !estado.importandoFoto)
            }

            item {
                Button(
                    onClick = estudiantesViewModel::registrar,
                    enabled = !estado.guardando && !estado.importandoFoto,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (estado.guardando) "Guardando…"
                        else "Guardar estudiante"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Listado de estudiantes (${estado.estudiantes.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (estado.estudiantes.isNotEmpty()) {
                item {
                    val ocupado = estado.guardando || estado.importandoFoto
                    OutlinedButton(
                        onClick = { editandoLista = !editandoLista; seleccionados = emptySet() },
                        enabled = !ocupado
                    ) { Text(if (editandoLista) "Cancelar edición" else "Editar estudiantes") }
                    if (editandoLista) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { seleccionados = estado.estudiantes.map { it.id }.toSet() }, enabled = !ocupado) {
                                Text("Seleccionar todos")
                            }
                            TextButton(onClick = { seleccionados = emptySet() }, enabled = !ocupado && seleccionados.isNotEmpty()) {
                                Text("Quitar selección")
                            }
                        }
                        Button(onClick = { confirmarRetiro = true }, enabled = !ocupado && seleccionados.isNotEmpty()) {
                            Text("Retirar seleccionados (${seleccionados.size})")
                        }
                    }
                }
            }

            if (estado.estudiantes.isEmpty()) {
                item {
                    Text("Todavía no hay estudiantes registrados en este curso.")
                }
            }

            items(
                items = estado.estudiantes,
                key = { it.id }
            ) { estudiante ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        if (editandoLista) {
                            Checkbox(
                                checked = estudiante.id in seleccionados,
                                enabled = !estado.guardando && !estado.importandoFoto,
                                onCheckedChange = { marcada ->
                                    seleccionados = if (marcada) seleccionados + estudiante.id else seleccionados - estudiante.id
                                },
                                modifier = Modifier.semantics { contentDescription = "Seleccionar ${estudiante.nombre}" }
                            )
                        }
                        ImagenInteligente(
                            ruta = estudiante.fotoRuta,
                            descripcion = "Fotografía de ${estudiante.nombre}"
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = estudiante.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (estudiante.rut == null) "Acceso pendiente de configurar" else "Acceso configurado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (!editandoLista) {
                        TextButton(onClick = { estudiantesViewModel.editarAcceso(estudiante.id) }) {
                            Text(if (estudiante.rut == null) "Configurar RUT y PIN" else "Cambiar RUT o PIN")
                        }
                    }
                }
            }
            if (estado.retirados.isNotEmpty()) {
                item {
                    OutlinedButton(onClick = { verRetirados = !verRetirados }, enabled = !estado.guardando) {
                        Text(if (verRetirados) "Ocultar estudiantes retirados" else "Ver estudiantes retirados (${estado.retirados.size})")
                    }
                }
                if (verRetirados) {
                    items(estado.retirados, key = { "retirado_${it.id}" }) { estudiante ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(estudiante.nombre, style = MaterialTheme.typography.titleMedium)
                                TextButton(onClick = { estudiantesViewModel.restaurarEstudiante(estudiante.id) },
                                    enabled = !estado.guardando && !estado.importandoFoto) { Text("Restaurar estudiante") }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun CamposAccesoEstudiante(
    rut: String, pin: String, error: String?,
    onRut: (String) -> Unit, onPin: (String) -> Unit, habilitado: Boolean
) {
    var visible by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = rut, onValueChange = onRut, label = { Text("RUT del estudiante") },
            singleLine = true, enabled = habilitado, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pin, onValueChange = onPin, label = { Text("PIN de 4 números") },
            singleLine = true, enabled = habilitado,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            trailingIcon = { IconButton(onClick = { visible = !visible }) {
                Icon(painterResource(R.drawable.ic_visibility), if (visible) "Ocultar PIN" else "Mostrar PIN")
            } }, modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

