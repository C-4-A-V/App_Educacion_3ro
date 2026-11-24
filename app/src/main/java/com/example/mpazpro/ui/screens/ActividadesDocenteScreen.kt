package com.example.mpazpro.ui.screens

import com.example.mpazpro.ui.theme.DocenteTheme
import com.example.mpazpro.ui.theme.EncabezadoDocente

import com.example.mpazpro.ui.components.BotonVolver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.model.EstadoPublicacionDocente
import com.example.mpazpro.ui.viewmodel.ActividadesDocenteViewModel

@Composable
internal fun ConfirmacionCompartirPreguntas(
    cantidad: Int,
    asignatura: String,
    curso: String?,
    habilitado: Boolean,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("¿Compartir preguntas?") },
        text = {
            Text(if (curso == null) "El curso ya no está disponible. Vuelve a elegirlo."
                else "Se compartirán $cantidad pregunta(s) de $asignatura con $curso.")
        },
        confirmButton = {
            TextButton(onClick = onConfirmar, enabled = habilitado) { Text("Confirmar y compartir") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
internal fun TarjetaPreguntaSoloServidor(pregunta: com.example.mpazpro.data.remote.ActividadRemotaEstudiante) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(pregunta.titulo, style = MaterialTheme.typography.titleMedium)
            Text(pregunta.pregunta)
            Text("Compartida · sin copia en este dispositivo", style = MaterialTheme.typography.labelMedium)
            if (pregunta.yaRespondida) Text("Ya respondida", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Disponible para consulta. Para editarla o compartirla con otro curso se necesita su copia de origen.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun AccionesSeleccionPreguntas(
    cantidad: Int,
    ocupado: Boolean,
    compartirDisponible: Boolean,
    puedeEditar: Boolean,
    onCompartir: () -> Unit,
    onEliminar: () -> Unit,
    onEditar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Button(onClick = onCompartir, modifier = Modifier.fillMaxWidth(),
            enabled = !ocupado && compartirDisponible && cantidad > 0) { Text("Compartir seleccionadas") }
        OutlinedButton(onClick = onEliminar, modifier = Modifier.fillMaxWidth(),
            enabled = !ocupado && cantidad > 0) { Text("Eliminar seleccionadas") }
        OutlinedButton(onClick = onEditar, modifier = Modifier.fillMaxWidth(),
            enabled = !ocupado && cantidad == 1 && puedeEditar) { Text("Editar seleccionada") }
    }
}

@Composable
private fun EditarPreguntaDialog(
    actividad: com.example.mpazpro.data.local.ActividadEntity,
    guardando: Boolean,
    aviso: String?,
    onCancelar: () -> Unit,
    onGuardar: (com.example.mpazpro.model.ActividadFormulario) -> Unit
) {
    var formulario by remember(actividad.id) {
        mutableStateOf(com.example.mpazpro.model.ActividadFormulario(
            titulo = actividad.titulo, instrucciones = actividad.instrucciones, pregunta = actividad.pregunta,
            alternativas = listOf(actividad.alternativaA, actividad.alternativaB, actividad.alternativaC, actividad.alternativaD),
            respuestaCorrecta = actividad.respuestaCorrecta, maxIntentos = actividad.maxIntentos,
            explicacion = actividad.explicacion, pista = actividad.pista
        ))
    }
    val errores = com.example.mpazpro.validation.ActividadValidator.validar(formulario.normalizado())
    AlertDialog(
        onDismissRequest = { if (!guardando) onCancelar() },
        title = { Text("Editar pregunta") },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Los cambios solo se aplican a este curso.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(formulario.titulo, { formulario = formulario.copy(titulo = it) }, label = { Text("Título") }, enabled = !guardando)
                OutlinedTextField(formulario.instrucciones, { formulario = formulario.copy(instrucciones = it) }, label = { Text("Instrucciones") }, enabled = !guardando)
                OutlinedTextField(formulario.pregunta, { formulario = formulario.copy(pregunta = it) }, label = { Text("Pregunta") }, enabled = !guardando)
                formulario.alternativas.forEachIndexed { indice, texto ->
                    OutlinedTextField(texto, { nuevo ->
                        formulario = formulario.copy(alternativas = formulario.alternativas.toMutableList().also { it[indice] = nuevo })
                    }, label = { Text("Alternativa ${"ABCD"[indice]}") }, enabled = !guardando)
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(formulario.respuestaCorrecta == indice, { formulario = formulario.copy(respuestaCorrecta = indice) }, enabled = !guardando)
                        Text("Respuesta correcta")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 3).forEach { cantidad ->
                        FilterChip(selected = formulario.maxIntentos == cantidad,
                            onClick = { formulario = formulario.copy(maxIntentos = cantidad) },
                            enabled = !guardando, label = { Text("$cantidad intento(s)") })
                    }
                }
                OutlinedTextField(formulario.explicacion, { formulario = formulario.copy(explicacion = it) }, label = { Text("Explicación") }, enabled = !guardando)
                OutlinedTextField(formulario.pista, { formulario = formulario.copy(pista = it) }, label = { Text("Pista") }, enabled = !guardando)
                if (errores != null) Text("Completa los campos y marca una respuesta válida.", style = MaterialTheme.typography.bodySmall)
                aviso?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = { onGuardar(formulario) }, enabled = !guardando && errores == null) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onCancelar, enabled = !guardando) { Text("Cancelar") } }
    )
}

@Composable
fun ActividadesDocenteScreen(
    cursoId: Long,
    docenteId: Long,
    asignatura: Asignatura,
    onVolver: () -> Unit,
    onCrearActividad: () -> Unit,
    onVerResultados: (Long) -> Unit,
    soloResultados: Boolean = false,
    modifier: Modifier = Modifier,
    actividadesViewModel: ActividadesDocenteViewModel = viewModel()
) {
    DocenteTheme {
    val estado by actividadesViewModel.uiState
        .collectAsStateWithLifecycle()
    var filtro by rememberSaveable(cursoId, asignatura.id) { mutableStateOf("Todas") }
    val actividadesVisibles = estado.actividades.filter { actividad ->
        when (filtro) {
            "Compartidas" -> estado.publicaciones[actividad.id] == EstadoPublicacionDocente.COMPARTIDA
            "No compartidas" -> estado.publicaciones[actividad.id] == EstadoPublicacionDocente.SOLO_LOCAL ||
                estado.publicaciones[actividad.id] == EstadoPublicacionDocente.PENDIENTE ||
                estado.publicaciones[actividad.id] == null
            else -> true
        }
    }
    var actividadEditada by remember(cursoId, asignatura.id) { mutableStateOf<com.example.mpazpro.data.local.ActividadEntity?>(null) }
    var seleccionando by remember(cursoId, asignatura.id) { mutableStateOf(false) }
    var seleccionadas by remember(cursoId, asignatura.id) { mutableStateOf<Set<Long>>(emptySet()) }
    var confirmarRetiro by remember(cursoId, asignatura.id) { mutableStateOf(false) }
    var elegirDestino by remember(cursoId, asignatura.id) { mutableStateOf(false) }
    var confirmarCompartir by remember(cursoId, asignatura.id) { mutableStateOf(false) }
    var destinoSeleccionado by remember(cursoId, asignatura.id) { mutableStateOf<Long?>(null) }
    val propiasNuevas = seleccionadas.count {
        estado.publicaciones[it] != EstadoPublicacionDocente.COMPARTIDA &&
            estado.publicaciones[it] != EstadoPublicacionDocente.OTRO_VINCULO
    }
    val hayOtroVinculo = seleccionadas.any { estado.publicaciones[it] == EstadoPublicacionDocente.OTRO_VINCULO }

    actividadEditada?.let { actividad ->
        EditarPreguntaDialog(actividad, estado.guardandoEdicion, estado.avisoPublicacion,
            onCancelar = { actividadEditada = null },
            onGuardar = { formulario ->
                actividadesViewModel.editarPregunta(actividad.id, formulario) {
                    actividadEditada = null; seleccionadas = emptySet(); seleccionando = false
                }
            })
    }

    if (elegirDestino) {
        AlertDialog(
            onDismissRequest = { elegirDestino = false; destinoSeleccionado = null },
            title = { Text("Elige el curso") },
            text = {
                Column(Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                    Text("Selecciona el destino para las preguntas marcadas.")
                    Row(
                        modifier = Modifier.fillMaxWidth().selectable(
                            selected = destinoSeleccionado == cursoId,
                            enabled = estado.cursoVinculado && propiasNuevas > 0 && !hayOtroVinculo,
                            role = Role.RadioButton,
                            onClick = { destinoSeleccionado = cursoId }
                        ).padding(vertical = 6.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(selected = destinoSeleccionado == cursoId, onClick = null,
                            enabled = estado.cursoVinculado && propiasNuevas > 0 && !hayOtroVinculo)
                        Column {
                            Text(estado.curso?.nombre?.let(com.example.mpazpro.data.repository.PropuestaVinculacionCursos::nombreVisible).orEmpty() + " (este curso)")
                            if (!estado.cursoVinculado) Text("Vincula este curso antes de compartir.", style = MaterialTheme.typography.bodySmall)
                            else if (hayOtroVinculo) Text("Una pregunta necesita revisar su vínculo.", style = MaterialTheme.typography.bodySmall)
                            else if (propiasNuevas == 0) Text("Las preguntas seleccionadas ya están compartidas.", style = MaterialTheme.typography.bodySmall)
                            else if (propiasNuevas < seleccionadas.size) Text("Se omitirán las que ya están compartidas.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    estado.cursosDestino.forEach { destino ->
                        Row(
                            modifier = Modifier.fillMaxWidth().selectable(
                                selected = destinoSeleccionado == destino.id,
                                enabled = destino.vinculado,
                                role = Role.RadioButton,
                                onClick = { destinoSeleccionado = destino.id }
                            ).padding(vertical = 6.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(selected = destinoSeleccionado == destino.id, onClick = null, enabled = destino.vinculado)
                            Column {
                                Text(com.example.mpazpro.data.repository.PropuestaVinculacionCursos.nombreVisible(destino.nombre))
                                if (!destino.vinculado) Text("Vincula este curso antes de compartir.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { elegirDestino = false; confirmarCompartir = true },
                    enabled = destinoSeleccionado != null) { Text("Continuar") }
            },
            dismissButton = { TextButton(onClick = { elegirDestino = false; destinoSeleccionado = null }) { Text("Cancelar") } }
        )
    }
    if (confirmarCompartir) {
        val destino = destinoSeleccionado
        val cursoElegido = estado.cursosDestino.find { it.id == destino && it.vinculado }
        val nombreDestino = if (destino == cursoId && estado.cursoVinculado) estado.curso?.nombre else cursoElegido?.nombre
        val cantidad = if (destino == cursoId) propiasNuevas else seleccionadas.size
        ConfirmacionCompartirPreguntas(
            cantidad = cantidad,
            asignatura = asignatura.titulo,
            curso = nombreDestino?.let(com.example.mpazpro.data.repository.PropuestaVinculacionCursos::nombreVisible),
            habilitado = destino != null && nombreDestino != null && cantidad > 0 &&
                estado.actividades.map { it.id }.containsAll(seleccionadas) && !(destino == cursoId && hayOtroVinculo),
            onCancelar = { confirmarCompartir = false; destinoSeleccionado = null },
            onConfirmar = {
                val ids = seleccionadas.toSet()
                confirmarCompartir = false
                if (destino != null) {
                    val alTerminar = { seleccionando = false; seleccionadas = emptySet() }
                    if (destino == cursoId) actividadesViewModel.compartirSeleccionActual(ids, alTerminar)
                    else actividadesViewModel.compartirEnOtroCurso(ids, destino, alTerminar)
                }
                destinoSeleccionado = null
            }
        )
    }
    if (confirmarRetiro) {
        AlertDialog(
            onDismissRequest = { confirmarRetiro = false },
            title = { Text("¿Eliminar preguntas?") },
            text = { Text("Se retirarán ${seleccionadas.size} preguntas de esta asignatura. Los estudiantes dejarán de verlas y se conservarán las respuestas anteriores.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarRetiro = false
                    actividadesViewModel.retirarSeleccion(seleccionadas) {
                        seleccionadas = emptySet()
                        seleccionando = false
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmarRetiro = false }) { Text("Cancelar") } }
        )
    }
    LaunchedEffect(cursoId, asignatura.id, docenteId) {
        actividadesViewModel.cargar(
            cursoId = cursoId,
            asignaturaId = asignatura.id,
            docenteId = docenteId
        )
    }

    if (!soloResultados) {
        ActualizacionAutomatica(Triple(cursoId, asignatura.id, docenteId)) {
            actividadesViewModel.actualizarServidorAutomaticamente()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BotonVolver(onClick = onVolver) {
                Text("Volver")
            }
        }

        item {
            EncabezadoDocente(
                titulo = if (soloResultados) {
                    "Ver resultados"
                } else {
                    "Actividades"
                },
                detalle = "Prepara propuestas y revisa el aprendizaje de tu curso."
            )
        }

        if (estado.cargando) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        estado.mensaje?.let { mensaje ->
            item {
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        val curso = estado.curso

        if (
            curso != null &&
            !estado.cargando &&
            estado.mensaje == null
        ) {
            item {
                Text(
                    text = asignatura.titulo,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Curso: ${com.example.mpazpro.data.repository.PropuestaVinculacionCursos.nombreVisible(curso.nombre)}",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (!soloResultados) {
                item {
                    Button(
                        onClick = onCrearActividad,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear actividad")
                    }
                    if (estado.actividades.isNotEmpty() || estado.preguntasServidor.isNotEmpty()) {
                        val ocupado = estado.retirando || estado.publicandoId != null ||
                            estado.preparandoBanco || estado.compartiendoPendientes ||
                            estado.enviandoOtroCurso || estado.guardandoEdicion
                        OutlinedButton(
                            onClick = { seleccionando = !seleccionando; seleccionadas = emptySet() },
                            enabled = !ocupado && estado.actividades.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (seleccionando) "Cancelar selección" else "Editar preguntas") }
                        if (estado.actividades.isEmpty()) {
                            Text("Las preguntas que solo están en el servidor son de consulta en este dispositivo.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        if (seleccionando) {
                            val elegida = estado.actividades.singleOrNull { it.id in seleccionadas }
                            val publicacion = elegida?.let { estado.publicaciones[it.id] }
                            val puedeEditar = seleccionadas.size == 1 && elegida != null &&
                                publicacion != EstadoPublicacionDocente.COMPARTIDA &&
                                publicacion != EstadoPublicacionDocente.OTRO_VINCULO &&
                                elegida.id !in estado.respondidasLocales
                            Text("Selecciona las preguntas guardadas en este dispositivo.", style = MaterialTheme.typography.bodySmall)
                            Text("Seleccionadas: ${seleccionadas.size}", style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { seleccionadas = actividadesVisibles.map { it.id }.toSet() },
                                    enabled = !ocupado && actividadesVisibles.isNotEmpty()) { Text("Seleccionar todas") }
                                TextButton(onClick = { seleccionadas = emptySet() },
                                    enabled = !ocupado && seleccionadas.isNotEmpty()) { Text("Quitar selección") }
                            }
                            AccionesSeleccionPreguntas(
                                cantidad = seleccionadas.size, ocupado = ocupado,
                                compartirDisponible = estado.publicacionDisponible, puedeEditar = puedeEditar,
                                onCompartir = { destinoSeleccionado = null; elegirDestino = true },
                                onEliminar = { confirmarRetiro = true },
                                onEditar = { actividadEditada = elegida }
                            )
                            if (seleccionadas.size > 1) {
                                Text("Para editar, selecciona solo una pregunta.", style = MaterialTheme.typography.bodySmall)
                            } else if (elegida != null && !puedeEditar) {
                                Text("No se pueden editar preguntas compartidas o respondidas.",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            if (!soloResultados) {
                item {
                    if (!estado.publicacionDisponible) {
                        Text("Compartir preguntas está disponible en la versión de prueba.")
                    } else if (!estado.cursoVinculado) {
                        Text("Para compartir preguntas, vuelve a Actividades, elige «Vincular curso» y conecta este curso.")
                    }
                    estado.avisoPublicacion?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                }
            }

            if (!soloResultados) {
                item {
                    Text("Filtrar preguntas", style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Todas", "Compartidas", "No compartidas").forEach { opcion ->
                            FilterChip(selected = filtro == opcion, onClick = {
                                filtro = opcion
                                seleccionadas = emptySet()
                            }, label = { Text(opcion) })
                        }
                    }
                    if (filtro == "No compartidas" && actividadesVisibles.any {
                            estado.publicaciones[it.id] == EstadoPublicacionDocente.PENDIENTE
                        }) {
                        Text("Incluye preguntas cuyo envío aún necesita confirmación.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (!soloResultados && filtro != "No compartidas" && estado.publicacionDisponible && estado.cursoVinculado) {
                item {
                    HorizontalDivider()
                    Text("Preguntas compartidas del curso", style = MaterialTheme.typography.titleMedium)
                    when {
                        estado.consultandoServidor -> LinearProgressIndicator(Modifier.fillMaxWidth())
                        estado.errorServidor -> {
                            Text("No pudimos consultar las preguntas compartidas. Las guardadas en este dispositivo siguen disponibles.")
                            TextButton(onClick = actividadesViewModel::actualizarServidor) { Text("Reintentar consulta") }
                        }
                        estado.consultaServidorRealizada && estado.preguntasServidor.isEmpty() ->
                            Text("Todavía no hay preguntas compartidas en esta asignatura.")
                        estado.consultaServidorRealizada -> {
                            Text("${estado.preguntasServidor.size} pregunta(s) compartidas en el servidor.")
                            Text("Las que tienen copia en este dispositivo aparecen en la lista de abajo.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (!estado.consultandoServidor && !estado.errorServidor) {
                        TextButton(onClick = actividadesViewModel::actualizarServidor) { Text("Actualizar preguntas compartidas") }
                    }
                }
                items(
                    items = estado.preguntasServidor.filter { it.id !in estado.idsPublicadosLocales },
                    key = { "servidor_${it.id}" }
                ) { pregunta ->
                    TarjetaPreguntaSoloServidor(pregunta)
                }
            }

            item {
                Text(
                    text = "Actividades guardadas en este dispositivo: ${estado.actividades.size}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (!soloResultados && actividadesVisibles.isEmpty() && estado.actividades.isNotEmpty() &&
                (filtro == "No compartidas" || estado.preguntasServidor.none { it.id !in estado.idsPublicadosLocales })) {
                item { Text("No hay preguntas para este filtro.") }
            }

            if (estado.actividades.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Sin actividades guardadas en este dispositivo",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = if (soloResultados) {
                                    "Todavía no hay actividades en esta asignatura."
                                } else {
                                    "Crea una actividad para esta asignatura o consulta las preguntas compartidas."
                                }
                            )
                        }
                    }
                }
            }

            items(
                items = if (soloResultados) estado.actividades else actividadesVisibles,
                key = { it.id }
            ) { actividad ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (seleccionando) {
                            val ocupado = estado.retirando || estado.publicandoId != null ||
                                estado.preparandoBanco || estado.compartiendoPendientes || estado.enviandoOtroCurso || estado.guardandoEdicion
                            Row(
                                modifier = Modifier.fillMaxWidth().selectable(
                                    selected = actividad.id in seleccionadas,
                                    enabled = !ocupado,
                                    role = Role.Checkbox,
                                    onClick = {
                                        seleccionadas = if (actividad.id in seleccionadas)
                                            seleccionadas - actividad.id else seleccionadas + actividad.id
                                    }
                                ),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Checkbox(checked = actividad.id in seleccionadas, enabled = !ocupado, onCheckedChange = null)
                                Text("Seleccionar ${actividad.titulo}")
                            }
                        }
                        Text(
                            text = actividad.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (actividad.id in estado.respondidasLocales ||
                            estado.preguntasServidor.any { it.id == estado.servidorPorActividad[actividad.id] && it.yaRespondida }) {
                            Text("Ya respondida", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = actividad.instrucciones,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = actividad.pregunta,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (!soloResultados && estado.publicacionDisponible) {
                            val publicacion = estado.publicaciones[actividad.id] ?: EstadoPublicacionDocente.SOLO_LOCAL
                            Text(
                                when (publicacion) {
                                    EstadoPublicacionDocente.SOLO_LOCAL -> "Guardada en este dispositivo"
                                    EstadoPublicacionDocente.PENDIENTE -> "Pendiente de confirmación del envío"
                                    EstadoPublicacionDocente.COMPARTIDA -> when {
                                        estado.consultaServidorRealizada &&
                                            estado.servidorPorActividad[actividad.id] !in estado.preguntasServidor.map { it.id } ->
                                            "No aparece en el servidor; requiere revisión"
                                        estado.consultaServidorRealizada -> "Compartida con el curso"
                                        else -> "Registrada como compartida en este dispositivo"
                                    }
                                    EstadoPublicacionDocente.OTRO_VINCULO -> "Publicación asociada a otro vínculo; requiere revisión"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        if (!seleccionando) {
                            TextButton(onClick = { onVerResultados(actividad.id) }) {
                                Text("Ver resultados anteriores")
                            }
                        }
                    }
                }
            }
        }
    }
    }
}
