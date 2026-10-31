package com.example.mpazpro.data.repository

import androidx.room.withTransaction
import com.example.mpazpro.data.local.ActividadEntity
import com.example.mpazpro.data.local.MpazDatabase
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

            database.actividadDao().insertar(
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
                    explicacion = datos.explicacion
                )
            )
        }
    }
}