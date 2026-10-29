package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mpazpro.model.ActividadEstudianteResumen
import com.example.mpazpro.model.ActividadParaResolver
import com.example.mpazpro.model.ProgresoEstudiante
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Insert
    suspend fun insertar(
        actividad: ActividadEntity
    ): Long

    @Query(
        """
        SELECT * FROM actividades
        WHERE id = :actividadId
        LIMIT 1
        """
    )
    suspend fun buscarPorId(actividadId: Long): ActividadEntity?

    @Query(
        """
        SELECT a.*
        FROM actividades a
        INNER JOIN cursos c ON c.id = a.cursoId
        WHERE a.cursoId = :cursoId
          AND a.asignaturaId = :asignaturaId
          AND c.docenteId = :docenteId
        ORDER BY a.creadaEn DESC, a.id DESC
        """
    )
    fun observarPorCursoYAsignatura(
        cursoId: Long,
        asignaturaId: String,
        docenteId: Long
    ): Flow<List<ActividadEntity>>

    @Query(
        """
        SELECT
            a.id,
            a.titulo,
            a.instrucciones,
            (i.id IS NOT NULL) AS resuelta
        FROM actividades a
        INNER JOIN estudiantes e ON e.cursoId = a.cursoId
        LEFT JOIN intentos_actividad i
            ON i.actividadId = a.id
            AND i.estudianteId = e.id
        WHERE e.id = :estudianteId
          AND a.asignaturaId = :asignaturaId
        ORDER BY a.creadaEn DESC, a.id DESC
        """
    )
    fun observarParaEstudiante(
        estudianteId: Long,
        asignaturaId: String
    ): Flow<List<ActividadEstudianteResumen>>

    @Query(
        """
        SELECT
            a.id,
            a.asignaturaId,
            a.titulo,
            a.instrucciones,
            a.pregunta,
            a.alternativaA,
            a.alternativaB,
            a.alternativaC,
            a.alternativaD
        FROM actividades a
        INNER JOIN estudiantes e ON e.cursoId = a.cursoId
        WHERE a.id = :actividadId
          AND e.id = :estudianteId
        LIMIT 1
        """
    )
    suspend fun buscarParaResolver(
        actividadId: Long,
        estudianteId: Long
    ): ActividadParaResolver?

    @Query(
        """
        SELECT a.*
        FROM actividades a
        INNER JOIN estudiantes e ON e.cursoId = a.cursoId
        WHERE a.id = :actividadId
          AND e.id = :estudianteId
        LIMIT 1
        """
    )
    suspend fun buscarParaEvaluar(
        actividadId: Long,
        estudianteId: Long
    ): ActividadEntity?

    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM actividades a INNER JOIN estudiantes e ON e.cursoId = a.cursoId WHERE e.id = :estudianteId) AS total,
            (SELECT COUNT(*) FROM intentos_actividad WHERE estudianteId = :estudianteId) AS completadas,
            (SELECT COUNT(*) FROM intentos_actividad WHERE estudianteId = :estudianteId AND correcta = 1) AS correctas
        """
    )
    fun observarProgresoEstudiante(estudianteId: Long): Flow<ProgresoEstudiante>

    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM actividades a INNER JOIN estudiantes e ON e.cursoId = a.cursoId WHERE e.id = :estudianteId AND a.asignaturaId = :asignaturaId) AS total,
            (SELECT COUNT(*) FROM intentos_actividad i INNER JOIN actividades a ON a.id = i.actividadId WHERE i.estudianteId = :estudianteId AND a.asignaturaId = :asignaturaId) AS completadas,
            (SELECT COUNT(*) FROM intentos_actividad i INNER JOIN actividades a ON a.id = i.actividadId WHERE i.estudianteId = :estudianteId AND a.asignaturaId = :asignaturaId AND i.correcta = 1) AS correctas
        """
    )
    fun observarProgresoAsignatura(
        estudianteId: Long,
        asignaturaId: String
    ): Flow<ProgresoEstudiante>
}
