package com.example.mpazpro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.mpazpro.ui.components.anchoFormulario
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.model.CrearActividadUiState
import com.example.mpazpro.ui.viewmodel.CrearActividadViewModel

@Composable
fun CrearActividadScreen(
    cursoId: Long,
    docenteId: Long,
    asignatura: Asignatura,
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    modifier: Modifier = Modifier,
    actividadViewModel: CrearActividadViewModel = viewModel()
) {
    val estado by actividadViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(cursoId, docenteId, asignatura.id) {
        actividadViewModel.cargar(cursoId, docenteId, asignatura.id)
    }
    LaunchedEffect(estado.guardado) { if (estado.guardado) onGuardado() }
    CrearActividadContenido(
        estado, asignatura, onVolver,
        actividadViewModel::actualizarTitulo, actividadViewModel::actualizarInstrucciones,
        actividadViewModel::actualizarPregunta, actividadViewModel::actualizarExplicacion,
        actividadViewModel::actualizarAlternativa, actividadViewModel::seleccionarRespuesta,
        actividadViewModel::actualizarMaxIntentos,
        { actividadViewModel.guardar(asignatura.id) }, modifier
    )
}

@Composable
internal fun CrearActividadContenido(
    estado: CrearActividadUiState,
    asignatura: Asignatura,
    onVolver: () -> Unit,
    onTitulo: (String) -> Unit,
    onInstrucciones: (String) -> Unit,
    onPregunta: (String) -> Unit,
    onExplicacion: (String) -> Unit,
    onAlternativa: (Int, String) -> Unit,
    onCorrecta: (Int) -> Unit,
    onModalidad: (Int) -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val f = estado.formulario
    val errores = estado.errores
    val editable = !estado.cargando && !estado.guardando && !estado.guardado
    LazyColumn(
        modifier.fillMaxSize().anchoFormulario().imePadding().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TextButton(onClick = onVolver, enabled = !estado.guardando) { Text("Volver") } }
        item { Text("Crear actividad", style = MaterialTheme.typography.headlineMedium) }
        item {
            AnimatedVisibility(estado.cargando || estado.guardando) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            AnimatedVisibility(estado.mensaje != null) {
                Text(estado.mensaje.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
        }
        if (estado.curso != null && !estado.cargando) {
            item {
                Text(estado.curso.nombre, style = MaterialTheme.typography.titleMedium)
                Text(asignatura.titulo)
            }
            item {
                Text("Tipo de actividad", style = MaterialTheme.typography.titleMedium)
                Column(Modifier.selectableGroup()) {
                    listOf(3 to "Práctica · hasta 3 intentos", 1 to "Evaluación · 1 intento").forEach { (limite, etiqueta) ->
                        Row(
                            Modifier.fillMaxWidth().selectable(
                                selected = f.maxIntentos == limite, enabled = editable,
                                role = Role.RadioButton, onClick = { onModalidad(limite) }
                            ).heightIn(min = 48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(f.maxIntentos == limite, null, enabled = editable)
                            Text(etiqueta, Modifier.padding(start = 8.dp))
                        }
                    }
                }
                errores.modalidad?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            item { CampoActividad(f.titulo, "Título de la actividad", editable, errores.titulo, onCambio = onTitulo) }
            item { CampoActividad(f.instrucciones, "Instrucciones para el estudiante", editable, errores.instrucciones, true, onInstrucciones) }
            item { CampoActividad(f.pregunta, "Pregunta", editable, errores.pregunta, true, onPregunta) }
            item {
                Text("Alternativas", style = MaterialTheme.typography.titleLarge)
                Text("Completa las cuatro y marca la respuesta correcta.")
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    f.alternativas.forEachIndexed { indice, texto ->
                        CampoActividad(texto, "Alternativa ${"ABCD"[indice]}", editable, errores.alternativas[indice]) {
                            onAlternativa(indice, it)
                        }
                        Row(
                            Modifier.fillMaxWidth().selectable(
                                selected = f.respuestaCorrecta == indice,
                                enabled = editable, role = Role.RadioButton, onClick = { onCorrecta(indice) }
                            ).heightIn(min = 48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(f.respuestaCorrecta == indice, null, enabled = editable)
                            Text("Marcar ${"ABCD"[indice]} como correcta", Modifier.padding(start = 8.dp))
                        }
                    }
                }
                AnimatedVisibility(errores.respuestaCorrecta != null) {
                    Text(errores.respuestaCorrecta.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
            }
            item {
                CampoActividad(f.explicacion, "Explicación de la respuesta correcta", editable, errores.explicacion, true, onExplicacion)
                Text("Se mostrará al acertar o terminar los intentos.", style = MaterialTheme.typography.bodySmall)
            }
            item {
                Button(onGuardar, enabled = editable, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(if (estado.guardando) "Guardando…" else "Guardar actividad")
                }
            }
        }
    }
}

@Composable
private fun CampoActividad(
    valor: String,
    etiqueta: String,
    habilitado: Boolean,
    error: String?,
    multilinea: Boolean = false,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor, onValueChange = onCambio, label = { Text(etiqueta) },
        enabled = habilitado, singleLine = !multilinea,
        minLines = if (multilinea) 2 else 1, maxLines = if (multilinea) 5 else 1,
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        trailingIcon = {
            if (error != null) Icon(painterResource(android.R.drawable.ic_dialog_alert), "Error en $etiqueta")
        },
        modifier = Modifier.fillMaxWidth().animateContentSize()
    )
}
