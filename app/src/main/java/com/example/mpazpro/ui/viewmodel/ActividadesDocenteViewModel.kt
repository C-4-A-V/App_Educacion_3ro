package com.example.mpazpro.ui.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.ActividadRepository
import com.example.mpazpro.data.repository.CursoRepository
import com.example.mpazpro.data.repository.ConsultaAulaDocente
import com.example.mpazpro.data.remote.ServicioAulaActividades
import com.example.mpazpro.data.repository.PublicadorActividad
import com.example.mpazpro.data.repository.RetiroActividadesRepository
import com.example.mpazpro.data.remote.ServicioRetiroPublicaciones
import com.example.mpazpro.data.remote.ServicioActividadesServidor
import com.example.mpazpro.data.remote.ServicioPublicaciones
import com.example.mpazpro.model.EstadoPublicacionDocente
import com.example.mpazpro.model.ActividadesDocenteUiState
import com.example.mpazpro.model.CursoDestinoActividad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActividadesDocenteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = MpazDatabase.obtenerInstancia(application)

    private val cursos = CursoRepository(database.cursoDao())
    private val actividades = ActividadRepository(database)
    private val publicador = PublicadorActividad(database, ServicioActividadesServidor.api, ServicioPublicaciones.api)
    private val consultaAula = ConsultaAulaDocente(database, ServicioActividadesServidor.api, ServicioAulaActividades.api)
    private val retiro = RetiroActividadesRepository(database, ServicioActividadesServidor.api, ServicioRetiroPublicaciones.api)
    private val esDepuracion = (application.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private val _uiState = MutableStateFlow(
        ActividadesDocenteUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var contexto: Triple<Long, String, Long>? = null
    private var observacionJob: Job? = null
    private var consultaServidorJob: Job? = null

    fun editarPregunta(id: Long, formulario: com.example.mpazpro.model.ActividadFormulario, alTerminar: () -> Unit) {
        val actual = contexto ?: return
        val estado = _uiState.value
        if (estado.guardandoEdicion || estado.retirando || estado.publicandoId != null ||
            estado.compartiendoPendientes || estado.enviandoOtroCurso) return
        _uiState.update { it.copy(guardandoEdicion = true, avisoPublicacion = null) }
        viewModelScope.launch {
            try {
                actividades.editarNoCompartida(id, actual.first, actual.second, actual.third, formulario)
                if (contexto == actual) {
                    _uiState.update { it.copy(avisoPublicacion = "Pregunta actualizada en este curso.") }
                    alTerminar()
                }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                if (contexto == actual) _uiState.update {
                    it.copy(avisoPublicacion = "No se pudo editar. Comprueba la conexión y que la pregunta no esté compartida ni respondida.")
                }
            } finally {
                if (contexto == actual) _uiState.update { it.copy(guardandoEdicion = false) }
            }
        }
    }

    fun actualizarServidorAutomaticamente() {
        if (consultaServidorJob?.isActive != true) actualizarServidor()
    }

    fun actualizarServidor() {
        val actual = contexto ?: return
        if (!esDepuracion || !_uiState.value.cursoVinculado) return
        consultaServidorJob?.cancel()
        _uiState.update { it.copy(consultandoServidor = true, errorServidor = false) }
        consultaServidorJob = viewModelScope.launch {
            try {
                val preguntas = consultaAula.listar(actual.first, actual.second, actual.third).orEmpty()
                if (contexto == actual) _uiState.update {
                    it.copy(preguntasServidor = preguntas, consultandoServidor = false,
                        consultaServidorRealizada = true, errorServidor = false)
                }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                if (contexto == actual) _uiState.update {
                    it.copy(consultandoServidor = false,
                        consultaServidorRealizada = false, errorServidor = true)
                }
            }
        }
    }

    fun compartirEnOtroCurso(ids: Set<Long>, destinoId: Long, alTerminar: () -> Unit) {
        val actual = contexto ?: return
        val estado = _uiState.value
        if (!esDepuracion || ids.isEmpty() || estado.retirando || estado.enviandoOtroCurso ||
            estado.publicandoId != null || estado.preparandoBanco || estado.compartiendoPendientes ||
            !estado.actividades.map { it.id }.containsAll(ids) ||
            estado.cursosDestino.none { it.id == destinoId && it.vinculado }) return
        _uiState.update { it.copy(enviandoOtroCurso = true, avisoPublicacion = null) }
        viewModelScope.launch {
            var compartidas = 0
            try {
                for (id in ids.sorted()) {
                    val copia = actividades.copiarParaCurso(id, actual.first, destinoId, actual.second, actual.third)
                    publicador.publicar(copia, actual.third)
                    compartidas++
                }
                if (contexto == actual) {
                    _uiState.update { it.copy(avisoPublicacion = "Preguntas compartidas en el otro curso: $compartidas.") }
                    alTerminar()
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (contexto == actual) _uiState.update {
                    it.copy(avisoPublicacion = "Se confirmaron $compartidas. Revisa la conexión y vuelve a intentar; las copias pendientes quedan en el curso de destino.")
                }
            } finally {
                if (contexto == actual) _uiState.update { it.copy(enviandoOtroCurso = false) }
            }
        }
    }

    fun retirarSeleccion(ids: Set<Long>, alTerminar: () -> Unit) {
        val actual = contexto ?: return
        val estado = _uiState.value
        if (ids.isEmpty() || estado.retirando || estado.enviandoOtroCurso || estado.publicandoId != null ||
            estado.preparandoBanco || estado.compartiendoPendientes ||
            !estado.actividades.map { it.id }.containsAll(ids)) return
        if (!esDepuracion && ids.any { estado.publicaciones[it] != EstadoPublicacionDocente.SOLO_LOCAL }) {
            _uiState.update { it.copy(avisoPublicacion = "La retirada de preguntas compartidas requiere la versión de prueba.") }
            return
        }
        _uiState.update { it.copy(retirando = true, avisoPublicacion = null) }
        viewModelScope.launch {
            try {
                retiro.retirar(actual.first, actual.second, actual.third, ids.toList())
                if (contexto == actual) {
                    _uiState.update { it.copy(avisoPublicacion = "Preguntas retiradas: ${ids.size}. Las respuestas anteriores se conservan.") }
                    actualizarServidor()
                    alTerminar()
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (contexto == actual) _uiState.update {
                    it.copy(avisoPublicacion = "No se pudo confirmar la retirada. Algunas preguntas podrían haberse retirado en el servidor; revisa la conexión y vuelve a intentarlo.")
                }
            } finally {
                if (contexto == actual) _uiState.update { it.copy(retirando = false) }
            }
        }
    }

    fun compartirSeleccionActual(ids: Set<Long>, alTerminar: () -> Unit) {
        val actual = contexto ?: return
        val estado = _uiState.value
        if (!esDepuracion || !estado.cursoVinculado || ids.isEmpty() ||
            estado.publicandoId != null || estado.preparandoBanco || estado.compartiendoPendientes ||
            estado.retirando || estado.enviandoOtroCurso ||
            !estado.actividades.map { it.id }.containsAll(ids) ||
            ids.any { estado.publicaciones[it] == EstadoPublicacionDocente.OTRO_VINCULO }) return
        val paraEnviar = ids.filter { estado.publicaciones[it] != EstadoPublicacionDocente.COMPARTIDA }.sorted()
        if (paraEnviar.isEmpty()) return
        _uiState.update { it.copy(compartiendoPendientes = true, avisoPublicacion = null) }
        viewModelScope.launch {
            var enviadas = 0
            try {
                publicador.publicarSeleccion(paraEnviar, actual.first, actual.second, actual.third) { enviadas++ }
                if (contexto == actual) {
                    _uiState.update { it.copy(avisoPublicacion = "Preguntas compartidas: $enviadas.") }
                    actualizarServidor()
                    alTerminar()
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (contexto == actual) _uiState.update {
                    it.copy(avisoPublicacion = "Se confirmaron $enviadas. Revisa la conexión y vuelve a intentar; las restantes no se confirmaron.")
                }
            } finally {
                if (contexto == actual) _uiState.update { it.copy(compartiendoPendientes = false) }
            }
        }
    }

    fun compartir(actividadId: Long) {
        val (cursoId, asignaturaId, docenteId) = contexto ?: return
        val estado = _uiState.value
        if (!esDepuracion || !estado.cursoVinculado || estado.publicandoId != null || estado.preparandoBanco || estado.compartiendoPendientes || estado.retirando || estado.enviandoOtroCurso ||
            estado.actividades.none { it.id == actividadId && it.cursoId == cursoId && it.asignaturaId == asignaturaId } ||
            estado.publicaciones[actividadId] == EstadoPublicacionDocente.COMPARTIDA ||
            estado.publicaciones[actividadId] == EstadoPublicacionDocente.OTRO_VINCULO) return
        _uiState.update { it.copy(publicandoId = actividadId, avisoPublicacion = null) }
        viewModelScope.launch {
            try {
                actividades.prepararPublicacion(actividadId, docenteId)
                publicador.publicar(actividadId, docenteId)
                if (contexto == Triple(cursoId, asignaturaId, docenteId))
                    _uiState.update { it.copy(avisoPublicacion = "Pregunta compartida con el curso.") }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (contexto == Triple(cursoId, asignaturaId, docenteId))
                    _uiState.update { it.copy(avisoPublicacion = "No se pudo compartir. Comprueba la conexión y vuelve a intentarlo.") }
            } finally {
                if (contexto == Triple(cursoId, asignaturaId, docenteId))
                    _uiState.update { it.copy(publicandoId = null) }
            }
        }
    }

    fun compartirPendientes() {
        val actual = contexto ?: return
        val estado = _uiState.value
        if (!esDepuracion || !estado.cursoVinculado || estado.publicandoId != null ||
            estado.preparandoBanco || estado.compartiendoPendientes || estado.retirando || estado.enviandoOtroCurso) return
        val ids = estado.actividades.map { it.id }.filter { estado.publicaciones[it] == EstadoPublicacionDocente.PENDIENTE }
        if (ids.isEmpty()) return
        _uiState.update { it.copy(compartiendoPendientes = true, avisoPublicacion = null) }
        viewModelScope.launch {
            var enviadas = 0
            try {
                for (id in ids) {
                    publicador.publicar(id, actual.third)
                    enviadas++
                }
                if (contexto == actual) _uiState.update {
                    it.copy(avisoPublicacion = "Preguntas compartidas: $enviadas.")
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (contexto == actual) _uiState.update {
                    it.copy(avisoPublicacion = "Se compartieron $enviadas. Las demás siguen pendientes; revisa la conexión y vuelve a intentarlo.")
                }
            } finally {
                if (contexto == actual) _uiState.update { it.copy(compartiendoPendientes = false) }
            }
        }
    }

    fun prepararBanco() {
        val (cursoId, asignaturaId, docenteId) = contexto ?: return
        if (!esDepuracion || !_uiState.value.cursoVinculado || _uiState.value.publicandoId != null || _uiState.value.preparandoBanco || _uiState.value.compartiendoPendientes || _uiState.value.retirando || _uiState.value.enviandoOtroCurso) return
        _uiState.update { it.copy(preparandoBanco = true, avisoPublicacion = null) }
        viewModelScope.launch {
            try {
                val cantidad = actividades.prepararBancoParaPublicacion(cursoId, docenteId, asignaturaId)
                if (contexto == Triple(cursoId, asignaturaId, docenteId))
                    _uiState.update { it.copy(avisoPublicacion = if (cantidad == 0) "No hay preguntas nuevas del banco para preparar."
                        else "Preguntas preparadas: $cantidad. Elige cuáles compartir.") }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (contexto == Triple(cursoId, asignaturaId, docenteId))
                    _uiState.update { it.copy(avisoPublicacion = "No se pudo preparar el banco. Revisa la vinculación del curso.") }
            } finally {
                if (contexto == Triple(cursoId, asignaturaId, docenteId)) _uiState.update { it.copy(preparandoBanco = false) }
            }
        }
    }

    fun cargar(
        cursoId: Long,
        asignaturaId: String,
        docenteId: Long
    ) {
        val nuevoContexto = Triple(
            cursoId,
            asignaturaId,
            docenteId
        )

        if (contexto == nuevoContexto) return

        observacionJob?.cancel()
        consultaServidorJob?.cancel()
        contexto = nuevoContexto
        _uiState.value = ActividadesDocenteUiState(publicacionDisponible = esDepuracion)

        observacionJob = viewModelScope.launch {
            try {
                val curso = cursos.obtenerCurso(
                    cursoId = cursoId,
                    docenteId = docenteId
                )

                if (curso == null) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensaje = "Curso no disponible."
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(curso = curso)
                }

                launch {
                    combine(database.cursoDao().observarPorDocente(docenteId),
                        database.vinculoCursoDao().observarPorDocente(docenteId)) { locales, vinculos ->
                        val idsVinculados = vinculos.map { it.cursoLocalId }.toSet()
                        locales.filter { it.id != cursoId }.map {
                            CursoDestinoActividad(it.id, it.nombre, it.id in idsVinculados)
                        }
                    }.collect { destinos ->
                        if (contexto == nuevoContexto) _uiState.update { it.copy(cursosDestino = destinos) }
                    }
                }
                launch {
                    database.intentoActividadDao().observarIdsRespondidas(cursoId, asignaturaId, docenteId)
                        .collect { ids ->
                            if (contexto == nuevoContexto) _uiState.update { it.copy(respondidasLocales = ids.toSet()) }
                        }
                }
                var ultimoVinculo: Pair<String, Long>? = null
                combine(
                    actividades.observarActividades(cursoId, asignaturaId, docenteId),
                    database.publicacionPendienteDao().observarPorAsignatura(cursoId, asignaturaId, docenteId),
                    database.vinculoCursoDao().observar(cursoId)
                ) { lista, publicaciones, vinculo ->
                    val porActividad = publicaciones.associateBy { it.actividadLocalId }
                    val estados = lista.associate { actividad ->
                        val publicacion = porActividad[actividad.id]
                        actividad.id to when {
                            publicacion == null -> EstadoPublicacionDocente.SOLO_LOCAL
                            vinculo == null || publicacion.catalogoId != vinculo.catalogoId ||
                                publicacion.cursoServidorId != vinculo.cursoServidorId -> EstadoPublicacionDocente.OTRO_VINCULO
                            publicacion.actividadServidorId == null -> EstadoPublicacionDocente.PENDIENTE
                            else -> EstadoPublicacionDocente.COMPARTIDA
                        }
                    }
                    val idsVisibles = lista.map { it.id }.toSet()
                    val porActividadServidor = publicaciones.filter {
                        it.actividadLocalId in idsVisibles && vinculo != null &&
                            it.catalogoId == vinculo.catalogoId && it.cursoServidorId == vinculo.cursoServidorId
                    }.mapNotNull { p -> p.actividadServidorId?.let { p.actividadLocalId to it } }.toMap()
                    val clave = vinculo?.let { it.catalogoId to it.cursoServidorId }
                    Triple(lista, estados, porActividadServidor to clave)
                }.collect { (lista, estados, detalle) ->
                    val (porActividadServidor, clave) = detalle
                    _uiState.update {
                        it.copy(actividades = lista, publicaciones = estados,
                            idsPublicadosLocales = porActividadServidor.values.toSet(),
                            servidorPorActividad = porActividadServidor,
                            cursoVinculado = clave != null, cargando = false, mensaje = null)
                    }
                    if (clave != ultimoVinculo) {
                        ultimoVinculo = clave
                        consultaServidorJob?.cancel()
                        if (clave != null) actualizarServidor()
                        else _uiState.update { it.copy(preguntasServidor = emptyList(), consultandoServidor = false,
                            consultaServidorRealizada = false, errorServidor = false) }
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensaje = "No se pudieron cargar las actividades."
                    )
                }
            }
        }
    }
}