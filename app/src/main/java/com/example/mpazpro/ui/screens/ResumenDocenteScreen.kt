package com.example.mpazpro.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.model.ProgresoEstudiante
import com.example.mpazpro.model.ResumenDocenteUiState
import com.example.mpazpro.model.ResultadoPreguntaDocente
import com.example.mpazpro.model.estadisticaPregunta
import com.example.mpazpro.ui.components.BotonVolver
import com.example.mpazpro.ui.components.ImagenInteligente
import com.example.mpazpro.ui.theme.DocenteTheme
import com.example.mpazpro.ui.theme.EncabezadoDocente
import com.example.mpazpro.ui.viewmodel.ResumenDocenteViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun ResumenDocenteScreen(
    docenteId: Long,
    cursoId: Long = 0L,
    estudianteId: Long = 0L,
    onVolver: () -> Unit,
    onAbrirCurso: (Long) -> Unit = {},
    onAbrirEstudiante: (Long) -> Unit = {},
    resumenViewModel: ResumenDocenteViewModel = viewModel()
) {
    val estado by resumenViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(docenteId) { resumenViewModel.cargar(docenteId) }
    DocenteTheme {
        ResumenDocenteContenido(
            estado = estado, cursoId = cursoId, estudianteId = estudianteId,
            onVolver = onVolver, onAbrirCurso = onAbrirCurso,
            onAbrirEstudiante = onAbrirEstudiante,
            onReintentar = { resumenViewModel.cargar(docenteId) },
            onEnviar = { actividadId, texto ->
                resumenViewModel.enviarMensaje(docenteId, estudianteId, actividadId, texto)
            }
        )
    }
}

@Composable
internal fun ResumenDocenteContenido(
    estado: ResumenDocenteUiState,
    cursoId: Long,
    estudianteId: Long,
    onVolver: () -> Unit,
    onAbrirCurso: (Long) -> Unit,
    onAbrirEstudiante: (Long) -> Unit,
    onReintentar: () -> Unit,
    onEnviar: suspend (Long, String) -> Unit
) {
    var vista by rememberSaveable(cursoId, estudianteId) { mutableStateOf("menu") }
    var asignaturaId by rememberSaveable(cursoId, estudianteId) { mutableStateOf<String?>(null) }
    var filtro by rememberSaveable(cursoId, estudianteId) { mutableStateOf("Todas") }
    var preguntaMensaje by remember(cursoId, estudianteId) { mutableStateOf<ResultadoPreguntaDocente?>(null) }
    var textoMensaje by rememberSaveable(cursoId, estudianteId) { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    var errorEnvio by remember { mutableStateOf<String?>(null) }
    var avisoEnvio by remember { mutableStateOf<String?>(null) }
    val alcance = rememberCoroutineScope()
    val lista = rememberLazyListState()
    val curso = estado.cursos.find { it.id == cursoId }
    val alumnos = estado.alumnos.filter { it.cursoId == cursoId }
    val estudiante = alumnos.find { it.id == estudianteId }
    val materia = Asignatura.buscarPorId(asignaturaId)

    val volver: () -> Unit = {
        when {
            asignaturaId != null -> { asignaturaId = null; filtro = "Todas" }
            estudianteId == 0L && vista != "menu" -> vista = "menu"
            else -> onVolver()
        }
    }
    BackHandler(enabled = asignaturaId != null || (estudianteId == 0L && vista != "menu")) { volver() }
    LaunchedEffect(vista, asignaturaId, estudianteId) { lista.scrollToItem(0) }

    preguntaMensaje?.let { pregunta ->
        AlertDialog(
            onDismissRequest = { if (!enviando) preguntaMensaje = null },
            title = { Text("Mensaje para ${estudiante?.nombre ?: "el estudiante"}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Actividad: ${pregunta.titulo}")
                    OutlinedTextField(
                        value = textoMensaje,
                        onValueChange = { if (it.length <= 500) textoMensaje = it },
                        label = { Text("Escribe tu retroalimentación") },
                        supportingText = { Text("${textoMensaje.length}/500") },
                        enabled = !enviando, minLines = 3, maxLines = 6
                    )
                    errorEnvio?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(enabled = textoMensaje.isNotBlank() && !enviando, onClick = {
                    enviando = true
                    errorEnvio = null
                    alcance.launch {
                        try {
                            onEnviar(pregunta.actividadId, textoMensaje)
                            preguntaMensaje = null
                            textoMensaje = ""
                            avisoEnvio = "Mensaje guardado para el estudiante en este dispositivo."
                        } catch (e: CancellationException) { throw e }
                        catch (_: Exception) { errorEnvio = "No se pudo guardar el mensaje. Inténtalo nuevamente." }
                        finally { enviando = false }
                    }
                }) { Text(if (enviando) "Guardando…" else "Enviar mensaje") }
            },
            dismissButton = {
                TextButton(enabled = !enviando, onClick = { preguntaMensaje = null }) { Text("Cancelar") }
            }
        )
    }

    LazyColumn(
        state = lista,
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BotonVolver(onClick = volver) {
                Text(when {
                    materia != null -> "Volver a asignaturas"
                    estudianteId != 0L -> "Volver a estudiantes"
                    vista != "menu" -> "Volver a opciones del curso"
                    else -> "Volver"
                })
            }
        }
        item {
            EncabezadoDocente(
                titulo = when {
                    materia != null -> materia.titulo
                    estudiante != null -> estudiante.nombre
                    curso != null -> "Resultados · ${curso.nombre}"
                    else -> "Resultados por curso"
                },
                detalle = when {
                    materia != null && estudiante != null -> "Respuestas de ${estudiante.nombre}"
                    materia != null -> "Respuestas generales del curso"
                    estudiante != null -> "Elige una asignatura para revisar sus respuestas."
                    curso != null -> "Elige cómo quieres revisar los resultados."
                    else -> "Selecciona un curso para revisar su avance."
                }
            )
        }

        when {
            estado.cargando -> item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            estado.error != null -> item {
                Text(estado.error)
                Button(onClick = onReintentar) { Text("Reintentar") }
            }
            cursoId == 0L -> {
                if (estado.cursos.isEmpty()) item { Text("Aún no tienes cursos.") }
                items(estado.cursos, key = { "curso_${it.id}" }) { actual ->
                    Card(onClick = { onAbrirCurso(actual.id) }, modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📚 ${actual.nombre}", style = MaterialTheme.typography.titleLarge)
                            Text("${estado.alumnos.count { it.cursoId == actual.id }} estudiantes")
                            Text("Ver resultados →", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            curso == null -> item { Text("Este curso no está disponible para tu cuenta.") }
            estudianteId == 0L && vista == "menu" -> {
                item {
                    OpcionResultados("👩‍🎓 Estudiantes", "Revisa las respuestas y envía retroalimentación individual.") {
                        vista = "estudiantes"
                    }
                }
                item {
                    OpcionResultados("📊 Respuestas en general", "Consulta los porcentajes por pregunta y asignatura.") {
                        vista = "general"
                    }
                }
            }
            estudianteId == 0L && vista == "estudiantes" -> {
                item { Text("Estudiantes del curso", style = MaterialTheme.typography.titleLarge) }
                if (alumnos.isEmpty()) item { Text("Este curso todavía no tiene estudiantes.") }
                items(alumnos, key = { "alumno_${it.id}" }) { alumno ->
                    Card(onClick = { onAbrirEstudiante(alumno.id) }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ImagenInteligente(alumno.fotoRuta, "Foto de ${alumno.nombre}", Modifier.size(52.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(alumno.nombre, style = MaterialTheme.typography.titleMedium)
                                Text("${alumno.general.completadas} de ${alumno.general.total} actividades completadas")
                                Text("Ver asignaturas →", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
            estudianteId != 0L && estudiante == null -> item { Text("Estudiante no disponible en este curso.") }
            materia == null -> {
                item {
                    Text(if (estudiante == null) "Asignaturas del curso" else "Asignaturas de ${estudiante.nombre}",
                        style = MaterialTheme.typography.titleLarge)
                }
                items(Asignatura.entries, key = { "materia_${it.id}" }) { actual ->
                    Card(onClick = { asignaturaId = actual.id; filtro = "Todas" },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(actual.titulo, style = MaterialTheme.typography.titleMedium)
                            if (estudiante != null) {
                                val progreso = estudiante.materias[actual.id] ?: ProgresoEstudiante()
                                Text("${progreso.completadas} de ${progreso.total} actividades completadas")
                            }
                            Text("Ver preguntas →", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            estudiante != null -> {
                val actividades = estado.actividades.filter { it.cursoId == cursoId && it.asignaturaId == materia.id }
                val resultados = estado.resultados.filter { it.cursoId == cursoId && it.estudianteId == estudianteId }
                    .associateBy { it.actividadId }
                val respuestas = actividades.mapIndexedNotNull { indice, actividad ->
                    resultados[actividad.id]?.let { (indice + 1) to it }
                }.filter { (_, resultado) ->
                    when (filtro) {
                        "Correctas" -> resultado.finalizada && resultado.correcta
                        "Incorrectas" -> !resultado.correcta
                        else -> true
                    }
                }
                avisoEnvio?.let { aviso -> item { Text(aviso) } }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Respuestas de ${estudiante.nombre}", style = MaterialTheme.typography.titleLarge)
                        Text("Las respuestas en curso se indican por separado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Todas", "Correctas", "Incorrectas").forEach { opcion ->
                            FilterChip(selected = filtro == opcion, onClick = { filtro = opcion }, label = { Text(opcion) })
                        }
                    }
                }
                item {
                    Text("${respuestas.size} ${if (respuestas.size == 1) "respuesta" else "respuestas"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (respuestas.isEmpty()) item { Text("No hay respuestas para mostrar con este filtro.") }
                items(respuestas, key = { "individual_${it.second.actividadId}" }) { (numero, pregunta) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("$numero. ${pregunta.titulo}", style = MaterialTheme.typography.titleMedium)
                            Text(pregunta.pregunta)
                            actividades.find { it.id == pregunta.actividadId }?.let { actividad ->
                                val alternativas = listOf(actividad.alternativaA, actividad.alternativaB,
                                    actividad.alternativaC, actividad.alternativaD)
                                alternativas.getOrNull(pregunta.respuestaElegida)?.let {
                                    Text("Última respuesta: $it")
                                }
                                alternativas.getOrNull(pregunta.respuestaCorrecta)?.let {
                                    Text("Respuesta correcta: $it")
                                }
                            }
                            Text(if (pregunta.maxIntentos == 1) "Una oportunidad" else "Hasta tres oportunidades",
                                style = MaterialTheme.typography.labelMedium)
                            val etiqueta = when {
                                !pregunta.finalizada && pregunta.tuvoError -> "En curso · ${pregunta.intentosUsados} intento(s) con error"
                                !pregunta.finalizada -> "En curso · ${pregunta.intentosUsados} intento(s)"
                                pregunta.correcta && pregunta.tuvoError -> "Correcta después de un error"
                                pregunta.correcta -> "Correcta"
                                else -> "Terminó sin acertar"
                            }
                            val colorEstado = when {
                                !pregunta.finalizada -> MaterialTheme.colorScheme.secondaryContainer
                                pregunta.correcta -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.errorContainer
                            }
                            val textoEstado = when {
                                !pregunta.finalizada -> MaterialTheme.colorScheme.onSecondaryContainer
                                pregunta.correcta -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onErrorContainer
                            }
                            Surface(shape = RoundedCornerShape(50), color = colorEstado, contentColor = textoEstado) {
                                Text(etiqueta, Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelLarge)
                            }
                            TextButton(onClick = {
                                textoMensaje = ""; errorEnvio = null; avisoEnvio = null; preguntaMensaje = pregunta
                            }) { Text("Escribir mensaje") }
                        }
                    }
                }
            }
            else -> {
                val actividades = estado.actividades.filter { it.cursoId == cursoId && it.asignaturaId == materia.id }
                val resultados = estado.resultados.filter { it.cursoId == cursoId }.groupBy { it.actividadId }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Panorama de la asignatura", style = MaterialTheme.typography.titleLarge)
                            val respondidas = actividades.count { resultados[it.id].orEmpty().isNotEmpty() }
                            Text("$respondidas de ${actividades.size} preguntas tienen respuestas",
                                style = MaterialTheme.typography.titleMedium)
                            Text("Los porcentajes usan solo a quienes respondieron cada pregunta.",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (actividades.isEmpty()) item { Text("No hay actividades en esta asignatura.") }
                items(actividades.withIndex().toList(), key = { "general_${it.value.id}" }) { (indice, actividad) ->
                    val filas = resultados[actividad.id].orEmpty()
                    val estadistica = estadisticaPregunta(filas)
                    val total = estadistica.respondieron
                    val correctas = estadistica.correctas
                    val incorrectas = estadistica.terminaronSinAcertar
                    val conError = estadistica.tuvieronErrores
                    val enCurso = estadistica.enCurso
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("${indice + 1}. ${actividad.titulo}", style = MaterialTheme.typography.titleMedium)
                            Text(actividad.pregunta, style = MaterialTheme.typography.bodyLarge)
                            Text(if (actividad.maxIntentos == 1) "Una oportunidad" else "Hasta tres oportunidades",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            if (total == 0) {
                                Text("Aún no hay respuestas.", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text("$total ${if (total == 1) "estudiante respondió" else "estudiantes respondieron"}",
                                    style = MaterialTheme.typography.labelLarge)
                                IndicadorResultado("Correctas", correctas, total, MaterialTheme.colorScheme.primary)
                                IndicadorResultado("Terminaron sin acertar", incorrectas, total, MaterialTheme.colorScheme.error)
                                IndicadorResultado("Tuvieron al menos un error", conError, total, MaterialTheme.colorScheme.tertiary)
                                if (enCurso > 0) {
                                    Text("En curso: $enCurso de $total", style = MaterialTheme.typography.bodyMedium)
                                }
                                Text("Un estudiante que acertó después de equivocarse aparece también en «al menos un error».",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IndicadorResultado(
    etiqueta: String,
    cantidad: Int,
    total: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("$etiqueta: ${porcentaje(cantidad, total)}% · $cantidad de $total",
            style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else cantidad.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(9.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
private fun OpcionResultados(titulo: String, detalle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleLarge)
            Text(detalle, style = MaterialTheme.typography.bodyMedium)
            Text("Continuar →", style = MaterialTheme.typography.labelLarge)
        }
    }
}

private fun porcentaje(parte: Int, total: Int): Int =
    if (total == 0) 0 else (parte * 100 + total / 2) / total
