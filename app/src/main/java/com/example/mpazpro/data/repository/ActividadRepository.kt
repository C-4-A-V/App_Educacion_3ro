package com.example.mpazpro.data.repository

import androidx.room.withTransaction
import com.example.mpazpro.data.local.ActividadEntity
import com.example.mpazpro.data.local.ActividadCopiaEntity
import com.example.mpazpro.data.local.BancoActividades
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.local.PublicacionPendienteEntity
import java.util.UUID
import com.example.mpazpro.model.ActividadFormulario
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.validation.ActividadValidator

class ActividadRepository(
    private val database: MpazDatabase
) {

    fun observarActividades(
        cursoId: Long,
        asignaturaId: String,
        docenteId: Long
    ) = database.actividadDao().observarPorCursoYAsignatura(
        cursoId = cursoId,
        asignaturaId = asignaturaId,
        docenteId = docenteId
    )

    /** Prepara explícitamente una actividad anterior sin duplicar su clave de envío. */
    suspend fun prepararPublicacion(actividadId: Long, docenteId: Long): PublicacionPendienteEntity =
        database.withTransaction {
            val actividad = database.actividadDao().buscarPorIdYDocente(actividadId, docenteId)
                ?: throw IllegalArgumentException("Actividad no disponible")
            check(!database.actividadRetiradaDao().existe(actividadId)) { "La actividad fue retirada" }
            val vinculo = database.vinculoCursoDao().buscar(actividad.cursoId)
                ?: throw IllegalStateException("El curso no está vinculado")
            val existente = database.publicacionPendienteDao().buscar(actividadId)
            if (existente != null) {
                check(existente.catalogoId == vinculo.catalogoId && existente.cursoServidorId == vinculo.cursoServidorId) {
                    "La publicación pertenece a otro vínculo"
                }
                existente
            } else {
                PublicacionPendienteEntity(
                    actividadLocalId = actividadId,
                    clavePublicacion = UUID.randomUUID().toString(),
                    catalogoId = vinculo.catalogoId,
                    cursoServidorId = vinculo.cursoServidorId
                ).also { database.publicacionPendienteDao().insertar(it) }
            }
        }

    /** Copia identificada por origen y destino; repetir no crea otra actividad ni cambia avances. */
    suspend fun copiarParaCurso(
        origenId: Long, cursoOrigenId: Long, cursoDestinoId: Long, asignaturaId: String, docenteId: Long
    ): Long = database.withTransaction {
        require(cursoOrigenId != cursoDestinoId)
        requireNotNull(database.cursoDao().buscarPorIdYDocente(cursoOrigenId, docenteId))
        requireNotNull(database.cursoDao().buscarPorIdYDocente(cursoDestinoId, docenteId))
        val origen = requireNotNull(database.actividadDao().buscarPorIdYDocente(origenId, docenteId))
        require(origen.cursoId == cursoOrigenId && origen.asignaturaId == asignaturaId)
        check(!database.actividadRetiradaDao().existe(origenId)) { "La pregunta de origen fue retirada" }
        val vinculo = requireNotNull(database.vinculoCursoDao().buscar(cursoDestinoId)) { "Curso destino sin vínculo" }
        val previa = database.actividadCopiaDao().buscar(origenId, cursoDestinoId)
        val copiaId = if (previa == null) {
            val id = database.actividadDao().insertar(origen.copy(id = 0, cursoId = cursoDestinoId, creadaEn = System.currentTimeMillis()))
            database.actividadCopiaDao().insertar(ActividadCopiaEntity(origenId, cursoDestinoId, id))
            id
        } else {
            val copia = requireNotNull(database.actividadDao().buscarPorId(previa.copiaId))
            check(!database.actividadRetiradaDao().existe(copia.id)) { "La copia de destino fue retirada" }
            check(copia.copy(id = 0, cursoId = 0, creadaEn = 0) == origen.copy(id = 0, cursoId = 0, creadaEn = 0)) {
                "La pregunta ha cambiado y requiere revisión"
            }
            copia.id
        }
        val pendiente = database.publicacionPendienteDao().buscar(copiaId)
        if (pendiente == null) {
            database.publicacionPendienteDao().insertar(PublicacionPendienteEntity(
                actividadLocalId = copiaId, clavePublicacion = UUID.randomUUID().toString(),
                catalogoId = vinculo.catalogoId, cursoServidorId = vinculo.cursoServidorId
            ))
        } else {
            check(pendiente.catalogoId == vinculo.catalogoId && pendiente.cursoServidorId == vinculo.cursoServidorId)
        }
        copiaId
    }

    /** Prepara solamente preguntas del banco aún no enviadas, sin modificar actividades ni avances. */
    suspend fun prepararBancoParaPublicacion(cursoId: Long, docenteId: Long, asignaturaId: String? = null): Int = database.withTransaction {
        requireNotNull(database.cursoDao().buscarPorIdYDocente(cursoId, docenteId)) { "Curso no disponible" }
        val vinculo = requireNotNull(database.vinculoCursoDao().buscar(cursoId)) { "Curso no vinculado" }
        var preparadas = 0
        for (original in BancoActividades.entidades(cursoId)) {
            if (asignaturaId != null && original.asignaturaId != asignaturaId) continue
            val id = database.actividadDao().buscarPredeterminada(cursoId, original.asignaturaId, original.titulo) ?: continue
            val guardada = database.actividadDao().buscarPorId(id) ?: continue
            if (database.actividadRetiradaDao().existe(id)) continue
            // Solo se preparan preguntas que conservan el contenido del banco.
            if (guardada.copy(id = 0, creadaEn = original.creadaEn, pista = original.pista) != original) continue
            if (database.publicacionPendienteDao().buscar(id) != null) continue
            database.publicacionPendienteDao().insertar(PublicacionPendienteEntity(
                actividadLocalId = id, clavePublicacion = UUID.randomUUID().toString(),
                catalogoId = vinculo.catalogoId, cursoServidorId = vinculo.cursoServidorId
            ))
            preparadas++
        }
        preparadas
    }

    suspend fun crearActividad(
        cursoId: Long,
        asignaturaId: String,
        docenteId: Long,
        formulario: ActividadFormulario
    ): Long {
        require(
            Asignatura.buscarPorId(asignaturaId) != null
        ) {
            "Asignatura no válida."
        }

        val datos = formulario.normalizado()
        val error = ActividadValidator.validar(datos)

        require(error == null) {
            error ?: "Revisa los datos."
        }

        return database.withTransaction {
            check(
                database.cursoDao().buscarPorIdYDocente(
                    cursoId = cursoId,
                    docenteId = docenteId
                ) != null
            ) {
                "Curso no disponible para este docente."
            }

            val actividadId = database.actividadDao().insertar(
                ActividadEntity(
                    cursoId = cursoId,
                    asignaturaId = asignaturaId,
                    titulo = datos.titulo,
                    instrucciones = datos.instrucciones,
                    pregunta = datos.pregunta,
                    alternativaA = datos.alternativas[0],
                    alternativaB = datos.alternativas[1],
                    alternativaC = datos.alternativas[2],
                    alternativaD = datos.alternativas[3],
                    respuestaCorrecta = datos.respuestaCorrecta,
                    maxIntentos = datos.maxIntentos,
                    explicacion = datos.explicacion,
                    pista = datos.pista
                )
            )
            database.vinculoCursoDao().buscar(cursoId)?.let { vinculo ->
                database.publicacionPendienteDao().insertar(
                    PublicacionPendienteEntity(
                        actividadLocalId = actividadId,
                        clavePublicacion = UUID.randomUUID().toString(),
                        catalogoId = vinculo.catalogoId,
                        cursoServidorId = vinculo.cursoServidorId
                    )
                )
            }
            actividadId
        }
    }
}