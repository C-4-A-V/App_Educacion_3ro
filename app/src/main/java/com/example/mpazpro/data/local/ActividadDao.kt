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

    @Query("SELECT id FROM actividades WHERE cursoId = :cursoId AND asignaturaId = :asignaturaId AND titulo = :titulo LIMIT 1")
    suspend fun buscarPredeterminada(cursoId: Long, asignaturaId: String, titulo: String): Long?

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
        WHERE a.id = :actividadId
          AND c.docenteId = :docenteId
        LIMIT 1
        """
    )
    suspend fun buscarPorIdYDocente(
        actividadId: Long,
        docenteId: Long
    ): ActividadEntity?

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
            COALESCE(i.finalizada, 0) AS resuelta,
            COALESCE(i.numero, 0) AS intentosUsados,
            a.maxIntentos
        FROM actividades a
        INNER JOIN estudiantes e ON e.cursoId = a.cursoId
        LEFT JOIN intentos_actividad i
            ON i.actividadId = a.id
            AND i.estudianteId = e.id
            AND i.numero = (SELECT MAX(ultimo.numero) FROM intentos_actividad ultimo WHERE ultimo.estudianteId = e.id AND ultimo.actividadId = a.id)
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
            a.alternativaD,
            a.maxIntentos
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
            (SELECT COUNT(*) FROM intentos_actividad WHERE estudianteId = :estudianteId AND finalizada = 1) AS completadas,
            (SELECT COUNT(*) FROM intentos_actividad WHERE estudianteId = :estudianteId AND finalizada = 1 AND correcta = 1) AS correctas
        """
    )
    fun observarProgresoEstudiante(estudianteId: Long): Flow<ProgresoEstudiante>

    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM actividades a INNER JOIN estudiantes e ON e.cursoId = a.cursoId WHERE e.id = :estudianteId AND a.asignaturaId = :asignaturaId) AS total,
            (SELECT COUNT(*) FROM intentos_actividad i INNER JOIN actividades a ON a.id = i.actividadId WHERE i.estudianteId = :estudianteId AND a.asignaturaId = :asignaturaId AND i.finalizada = 1) AS completadas,
            (SELECT COUNT(*) FROM intentos_actividad i INNER JOIN actividades a ON a.id = i.actividadId WHERE i.estudianteId = :estudianteId AND a.asignaturaId = :asignaturaId AND i.finalizada = 1 AND i.correcta = 1) AS correctas
        """
    )
    fun observarProgresoAsignatura(
        estudianteId: Long,
        asignaturaId: String
    ): Flow<ProgresoEstudiante>
}
