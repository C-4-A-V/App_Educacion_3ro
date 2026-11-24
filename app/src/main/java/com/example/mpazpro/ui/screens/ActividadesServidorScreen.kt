package com.example.mpazpro.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.data.remote.ActividadServidor
import com.example.mpazpro.data.remote.CursoCompartidoServidor
import com.example.mpazpro.data.remote.ServicioActividadesServidor
import com.example.mpazpro.data.remote.ServicioCrearCursoConfirmado
import com.example.mpazpro.data.remote.ServicioPublicaciones
import com.example.mpazpro.data.remote.ServicioAulaActividades
import com.example.mpazpro.data.remote.ActividadRemotaEstudiante
import com.example.mpazpro.data.repository.PublicadorActividad
import com.example.mpazpro.data.repository.ActividadRepository
import com.example.mpazpro.ui.theme.DocenteTheme
import androidx.lifecycle.AndroidViewModel
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.CursoParaVincular
import com.example.mpazpro.data.repository.PropuestaVinculacionCursos
import com.example.mpazpro.data.repository.VinculacionCursoRepository
import kotlinx.coroutines.flow.first
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

sealed interface EstadoServidor {
    data object Cargando : EstadoServidor
    data object Error : EstadoServidor
    data class Listo(val actividades: List<ActividadServidor>) : EstadoServidor
}

sealed interface EstadoCursosServidor {
    data object Cargando : EstadoCursosServidor
    data object Error : EstadoCursosServidor
    data class Listo(val cursos: List<CursoCompartidoServidor>) : EstadoCursosServidor
}

data class ActividadLocalPublicacion(val id: Long, val titulo: String, val pregunta: String, val estado: String)

class ActividadesServidorViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MpazDatabase.obtenerInstancia(application)
    var propuestas by mutableStateOf<List<CursoPropuesto>>(emptyList())
        private set
    var avisoVinculos by mutableStateOf<String?>(null)
        private set
    var nombresCursosLocales by mutableStateOf<List<String>?>(null)
        private set
    var trabajandoVinculos by mutableStateOf(false)
        private set
    private var docenteActual: Long? = null
    var actividadesLocales by mutableStateOf<List<ActividadLocalPublicacion>>(emptyList())
        private set
    private var consultaActual: Pair<Long, String>? = null
    var preguntasPublicadas by mutableStateOf<List<ActividadRemotaEstudiante>>(emptyList())
        private set
    var tituloPreguntas by mutableStateOf<String?>(null)
        private set
    var avisoPreguntas by mutableStateOf<String?>(null)
        private set
    var consultandoPreguntas by mutableStateOf(false)
        private set

    fun consultarPublicadas(docenteId: Long, cursoId: Long, asignatura: String) {
        if (consultandoPreguntas) return
        consultandoPreguntas = true
        avisoPreguntas = null
        preguntasPublicadas = emptyList()
        actividadesLocales = emptyList()
        consultaActual = cursoId to asignatura
        viewModelScope.launch {
            try {
                val curso = database.cursoDao().buscarPorIdYDocente(cursoId, docenteId)
                    ?: throw IllegalStateException("Curso no disponible")
                tituloPreguntas = "${PropuestaVinculacionCursos.nombreVisible(curso.nombre)} · ${asignatura.replaceFirstChar { it.uppercase() }}"
                actividadesLocales = database.actividadDao()
                    .observarPorCursoYAsignatura(cursoId, asignatura, docenteId).first()
                    .map { actividad ->
                        val publicacion = database.publicacionPendienteDao().buscar(actividad.id)
                        val estado = when {
                            publicacion == null -> "local"
                            publicacion.actividadServidorId == null -> "pendiente"
                            else -> "enviada"
                        }
                        ActividadLocalPublicacion(actividad.id, actividad.titulo, actividad.pregunta, estado)
                    }
                val vinculo = database.vinculoCursoDao().buscar(cursoId)
                    ?: throw IllegalStateException("Curso sin vínculo")
                val identidad = ServicioActividadesServidor.api.identidadCatalogo()
                check(identidad.persistente && identidad.identificador == vinculo.catalogoId)
                preguntasPublicadas = ServicioAulaActividades.api.listar(vinculo.catalogoId, vinculo.cursoServidorId, asignatura)
                tituloPreguntas = "${PropuestaVinculacionCursos.nombreVisible(curso.nombre)} · ${asignatura.replaceFirstChar { it.uppercase() }}"
                if (preguntasPublicadas.isEmpty()) avisoPreguntas = "Todavía no hay preguntas enviadas en esta asignatura."
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                avisoPreguntas = "No pudimos consultar el servidor. Las actividades locales siguen guardadas."
            } finally {
                consultandoPreguntas = false
            }
        }
    }

    var enviandoPendientes by mutableStateOf(false)
        private set
    var avisoEnvio by mutableStateOf<String?>(null)
        private set

    fun prepararBanco(docenteId: Long, cursoId: Long) {
        if (enviandoPendientes) return
        enviandoPendientes = true
        avisoEnvio = null
        viewModelScope.launch {
            try {
                val cantidad = ActividadRepository(database).prepararBancoParaPublicacion(cursoId, docenteId)
                avisoEnvio = if (cantidad == 0) "No hay preguntas del banco nuevas para preparar."
                    else "Preguntas del banco preparadas: $cantidad. Puedes enviarlas cuando quieras."
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                avisoEnvio = "No se pudieron preparar las preguntas. Comprueba que el curso esté vinculado."
            } finally {
                enviandoPendientes = false
                consultaActual?.let { (curso, asignatura) -> consultarPublicadas(docenteId, curso, asignatura) }
            }
        }
    }

    fun enviarActividadLocal(docenteId: Long, actividadId: Long) {
        if (enviandoPendientes) return
        enviandoPendientes = true
        avisoEnvio = null
        viewModelScope.launch {
            try {
                ActividadRepository(database).prepararPublicacion(actividadId, docenteId)
                PublicadorActividad(database, ServicioActividadesServidor.api, ServicioPublicaciones.api)
                    .publicar(actividadId, docenteId)
                avisoEnvio = "Actividad enviada al servidor."
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                avisoEnvio = "No se pudo enviar. Si se preparó correctamente, sigue pendiente en este dispositivo."
            } finally {
                enviandoPendientes = false
                consultaActual?.let { (curso, asignatura) -> consultarPublicadas(docenteId, curso, asignatura) }
            }
        }
    }

    fun enviarPendientes(docenteId: Long) {
        if (enviandoPendientes) return
        enviandoPendientes = true
        avisoEnvio = null
        viewModelScope.launch {
            var enviadas = 0
            try {
                val pendientes = database.publicacionPendienteDao().pendientesPorDocente(docenteId)
                val publicador = PublicadorActividad(database, ServicioActividadesServidor.api, ServicioPublicaciones.api)
                for (pendiente in pendientes) {
                    publicador.publicar(pendiente.actividadLocalId, docenteId)
                    enviadas++
                }
                avisoEnvio = if (enviadas == 0) "No hay actividades pendientes." else "Actividades enviadas: $enviadas."
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                avisoEnvio = "No se pudieron enviar todas. Las restantes siguen guardadas en este dispositivo."
            } finally {
                enviandoPendientes = false
            }
        }
    }

    fun revisarVinculos(docenteId: Long) {
        if (trabajandoVinculos) return
        docenteActual = docenteId
        trabajandoVinculos = true
        viewModelScope.launch {
            try {
                val identidad = ServicioActividadesServidor.api.identidadCatalogo()
                val remotos = ServicioActividadesServidor.api.listarCursos()
                val locales = database.cursoDao().observarPorDocente(docenteId).first()
                nombresCursosLocales = locales.map { PropuestaVinculacionCursos.nombreVisible(it.nombre) }
                val resultado = PropuestaVinculacionCursos.comparar(
                    locales.map { CursoParaVincular(it.id, it.nombre) },
                    remotos.map { CursoParaVincular(it.id, it.nombre) }
                )
                propuestas = resultado.propuestas.map { propuesta ->
                    val local = locales.first { it.id == propuesta.cursoLocalId }
                    val vinculo = database.vinculoCursoDao().buscar(local.id)
                    val estado = when {
                        vinculo == null && !identidad.persistente -> "temporal"
                        vinculo == null -> "disponible"
                        vinculo.catalogoId == identidad.identificador && vinculo.cursoServidorId == propuesta.cursoServidorId -> "vinculado"
                        else -> "otro"
                    }
                    CursoPropuesto(local.id, propuesta.cursoServidorId, local.nombre, estado)
                }
                avisoVinculos = when {
                    !identidad.persistente -> "Este catálogo es temporal; no se pueden guardar vínculos."
                    resultado.seccionesAmbiguas.isNotEmpty() -> "Hay cursos con nombres ambiguos que necesitan revisión."
                    locales.isEmpty() -> "Esta cuenta no tiene cursos locales en este dispositivo."
                    propuestas.isEmpty() -> "Los nombres de tus cursos locales no coinciden con los cursos del servidor."
                    else -> null
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                propuestas = emptyList()
                nombresCursosLocales = null
                avisoVinculos = "No pudimos revisar los cursos. Comprueba la conexión."
            } finally {
                trabajandoVinculos = false
            }
        }
    }

    fun vincularCurso(propuesta: CursoPropuesto) {
        val docenteId = docenteActual ?: return
        if (trabajandoVinculos || propuesta.estado != "disponible") return
        trabajandoVinculos = true
        viewModelScope.launch {
            try {
                VinculacionCursoRepository(database, ServicioActividadesServidor.api)
                    .vincular(docenteId, propuesta.localId, propuesta.remotoId)
                trabajandoVinculos = false
                revisarVinculos(docenteId)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                trabajandoVinculos = false
                avisoVinculos = "No se pudo vincular. Vuelve a revisar los cursos."
            }
        }
    }

    var estado by mutableStateOf<EstadoServidor>(EstadoServidor.Cargando)
        private set

    var cursosEstado by mutableStateOf<EstadoCursosServidor>(EstadoCursosServidor.Cargando)
        private set
    private var iniciado = false
    private var cursosEnCurso = false

    init {
        consultar()
        consultarCursos()
    }

    fun consultarCursos() {
        if (cursosEnCurso) return
        cursosEnCurso = true
        cursosEstado = EstadoCursosServidor.Cargando
        viewModelScope.launch {
            try {
                cursosEstado = EstadoCursosServidor.Listo(ServicioActividadesServidor.api.listarCursos())
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                cursosEstado = EstadoCursosServidor.Error
            } finally {
                cursosEnCurso = false
            }
        }
    }

    fun crearCursoCompartido(seccionConfirmada: Char) {
        val cursos = (cursosEstado as? EstadoCursosServidor.Listo)?.cursos ?: return
        if (cursosEnCurso || PropuestaVinculacionCursos.siguiente(cursos.map { it.nombre }) != seccionConfirmada) return
        cursosEnCurso = true
        cursosEstado = EstadoCursosServidor.Cargando
        viewModelScope.launch {
            try {
                ServicioCrearCursoConfirmado.api.crear(seccionConfirmada.toString())
                cursosEstado = EstadoCursosServidor.Listo(ServicioActividadesServidor.api.listarCursos())
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Ante una respuesta perdida, obliga a consultar antes de volver a crear.
                cursosEstado = EstadoCursosServidor.Error
            } finally {
                cursosEnCurso = false
            }
        }
    }

    fun consultar() {
        if (estado == EstadoServidor.Cargando && iniciado) return
        iniciado = true
        estado = EstadoServidor.Cargando
        viewModelScope.launch {
            estado = try {
                EstadoServidor.Listo(ServicioActividadesServidor.api.listar())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("ServidorPrueba", "Error al consultar las actividades", e)
                EstadoServidor.Error
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadesServidorScreen(docenteId: Long, onVolver: () -> Unit, modelo: ActividadesServidorViewModel = viewModel()) {
    LaunchedEffect(docenteId) { modelo.revisarVinculos(docenteId) }
    var confirmarCursoCompartido by remember { mutableStateOf<Char?>(null) }
    confirmarCursoCompartido?.let { seccion ->
        AlertDialog(
            onDismissRequest = { confirmarCursoCompartido = null },
            title = { Text("¿Crear nuevo curso compartido?") },
            text = { Text("Se creará 3.º Básico $seccion en el servidor.") },
            confirmButton = {
                TextButton(onClick = { confirmarCursoCompartido = null; modelo.crearCursoCompartido(seccion) }) { Text("Crear curso") }
            },
            dismissButton = { TextButton(onClick = { confirmarCursoCompartido = null }) { Text("Cancelar") } }
        )
    }
    DocenteTheme {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Actividades del servidor") },
                navigationIcon = { TextButton(onClick = onVolver) { Text("Volver") } }
            )
        }) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Prueba de conexión. Estas actividades están separadas de las de tus cursos.")
                }
                item {
                    HorizontalDivider()
                    Text("Cursos compartidos · prueba", style = MaterialTheme.typography.titleMedium)
                    Text("Catálogo del servidor, aún sin vincular a tus cursos y estudiantes.")
                }
                when (val cursos = modelo.cursosEstado) {
                    EstadoCursosServidor.Cargando -> item { CircularProgressIndicator() }
                    EstadoCursosServidor.Error -> item {
                        Text("No pudimos consultar los cursos. Actualiza antes de intentar crear otro.")
                        OutlinedButton(onClick = modelo::consultarCursos) { Text("Consultar cursos") }
                    }
                    is EstadoCursosServidor.Listo -> {
                        items(cursos.cursos, key = { "curso_${it.id}" }) { curso ->
                            Text(PropuestaVinculacionCursos.nombreVisible(curso.nombre))
                        }
                        item {
                            val siguiente = PropuestaVinculacionCursos.siguiente(cursos.cursos.map { it.nombre })
                            Button(onClick = { confirmarCursoCompartido = siguiente }, enabled = siguiente != null) { Text("Crear nuevo curso") }
                            if (siguiente == null) Text("No se puede determinar la siguiente sección.")
                            OutlinedButton(onClick = modelo::consultarCursos) { Text("Actualizar cursos") }
                        }
                    }
                }
                item {
                    HorizontalDivider()
                    Text("Vincular cursos existentes", style = MaterialTheme.typography.titleMedium)
                    Text("Revisa la coincidencia antes de vincular; se conservan los estudiantes y avances.")
                    modelo.avisoVinculos?.let { Text(it) }
                    modelo.nombresCursosLocales?.takeIf { it.isNotEmpty() }?.let { nombres ->
                        Text("Cursos locales de esta cuenta: " + nombres.joinToString(", "))
                    }
                    OutlinedButton(onClick = { modelo.revisarVinculos(docenteId) }, enabled = !modelo.trabajandoVinculos) {
                        Text("Revisar coincidencias")
                    }
                }
                items(modelo.propuestas, key = { "vinculo_${it.localId}" }) { propuesta ->
                    Column {
                        TarjetaCursoPropuesto(propuesta, modelo.trabajandoVinculos) {
                            modelo.vincularCurso(propuesta)
                        }
                        if (propuesta.estado == "vinculado") {
                            Text("Consultar preguntas enviadas:")
                            listOf(listOf("lenguaje", "matematica"), listOf("ciencias", "historia")).forEach { fila ->
                                Row {
                                    fila.forEach { asignatura ->
                                        TextButton(
                                            onClick = { modelo.consultarPublicadas(docenteId, propuesta.localId, asignatura) },
                                            enabled = !modelo.consultandoPreguntas
                                        ) { Text(asignatura.replaceFirstChar { it.uppercase() }) }
                                    }
                                }
                            }
                        }
                    }
                }
                if (modelo.tituloPreguntas != null || modelo.avisoPreguntas != null) {
                    item {
                        HorizontalDivider()
                        modelo.tituloPreguntas?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                        modelo.avisoPreguntas?.let { Text(it) }
                        Unit
                    }
                    items(modelo.preguntasPublicadas, key = { "publicada_${it.id}" }) { pregunta ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(pregunta.titulo, style = MaterialTheme.typography.titleMedium)
                                Text(pregunta.pregunta)
                            }
                        }
                    }
                    item {
                        Text("Actividades de este curso guardadas en el dispositivo", style = MaterialTheme.typography.titleMedium)
                        if (modelo.actividadesLocales.isEmpty()) Text("No hay actividades locales en esta asignatura.")
                    }
                    items(modelo.actividadesLocales, key = { "local_${it.id}" }) { actividad ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(actividad.titulo, style = MaterialTheme.typography.titleMedium)
                                Text(actividad.pregunta)
                                when (actividad.estado) {
                                    "enviada" -> Text("Enviada al servidor")
                                    "pendiente" -> Text("Pendiente de envío")
                                    else -> Text("Guardada solo en este dispositivo")
                                }
                                if (actividad.estado != "enviada") {
                                    Text("Para compartir, selecciona la pregunta en Actividades.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                item {
                    HorizontalDivider()
                    Text("Actividades pendientes de envío", style = MaterialTheme.typography.titleMedium)
                    Text("Para compartir, entra en Actividades, elige el curso y la asignatura, y selecciona las preguntas.")
                    modelo.avisoEnvio?.let { Text(it) }
                }
                item {
                    HorizontalDivider()
                    Text("Actividades de prueba", style = MaterialTheme.typography.titleMedium)
                }
                when (val estado = modelo.estado) {
                    EstadoServidor.Cargando -> item {
                        CircularProgressIndicator()
                        Text("Consultando el servidor…")
                    }
                    EstadoServidor.Error -> item {
                        Text("No pudimos conectar. Comprueba que Spring esté iniciado y vuelve a intentarlo.")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = modelo::consultar) { Text("Intentar de nuevo") }
                    }
                    is EstadoServidor.Listo -> {
                        if (estado.actividades.isEmpty()) {
                            item { Text("Todavía no hay actividades en el servidor.") }
                        }
                        items(estado.actividades) { actividad ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(actividad.titulo, style = MaterialTheme.typography.titleMedium)
                                    Text(actividad.asignatura, style = MaterialTheme.typography.labelLarge)
                                    Text(actividad.pregunta)
                                }
                            }
                        }
                        item { OutlinedButton(onClick = modelo::consultar) { Text("Actualizar") } }
                    }
                }
            }
        }
    }
}
