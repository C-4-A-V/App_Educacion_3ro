package com.example.mpazpro.data.repository

import androidx.room.withTransaction
import com.example.mpazpro.data.local.IntentoActividadEntity
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.model.ActividadParaResolver
import com.example.mpazpro.model.Asignatura

class ActividadEstudianteRepository(
    private val database: MpazDatabase
) {

    private val actividades = database.actividadDao()
    private val intentos = database.intentoActividadDao()

    fun observarActividades(
        estudianteId: Long,
        asignaturaId: String
    ) = actividades.observarParaEstudiante(
        estudianteId = estudianteId,
        asignaturaId = asignaturaId
    )

    fun observarProgresoAsignatura(
        estudianteId: Long,
        asignaturaId: String
    ) = actividades.observarProgresoAsignatura(
        estudianteId = estudianteId,
        asignaturaId = asignaturaId
    )

    suspend fun obtenerActividad(
        estudianteId: Long,
        actividadId: Long
    ): Pair<ActividadParaResolver, IntentoActividadEntity?>? {
        return database.withTransaction {
            val actividad = actividades.buscarParaResolver(
                actividadId = actividadId,
                estudianteId = estudianteId
            ) ?: return@withTransaction null

            val intento = intentos.buscar(
                estudianteId = estudianteId,
                actividadId = actividadId
            )

            actividad to intento
        }
    }

    suspend fun enviarRespuesta(
        estudianteId: Long,
        actividadId: Long,
        respuestaElegida: Int
    ): IntentoActividadEntity {
        require(respuestaElegida in 0..3) {
            "Selecciona una alternativa válida."
        }

        return database.withTransaction {
            val actividad = actividades.buscarParaEvaluar(
                actividadId = actividadId,
                estudianteId = estudianteId
            ) ?: error(
                "Esta actividad no está disponible para el estudiante."
            )

            val anterior = intentos.buscar(
                estudianteId = estudianteId,
                actividadId = actividadId
            )

            if (anterior != null) {
                return@withTransaction anterior
            }

            check(
                Asignatura.buscarPorId(actividad.asignaturaId) != null &&
                    actividad.respuestaCorrecta in 0..3
            ) {
                "La actividad contiene datos inválidos."
            }

            val intento = IntentoActividadEntity(
                estudianteId = estudianteId,
                actividadId = actividadId,
                respuestaElegida = respuestaElegida,
                respuestaCorrecta = actividad.respuestaCorrecta,
                correcta = respuestaElegida == actividad.respuestaCorrecta
            )

            val id = intentos.insertar(intento)

            if (id != -1L) {
                intento.copy(id = id)
            } else {
                intentos.buscar(
                    estudianteId = estudianteId,
                    actividadId = actividadId
                ) ?: error("No se pudo recuperar el intento.")
            }
        }
    }
}
