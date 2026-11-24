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

    @Query("""
        SELECT a.* FROM actividades a JOIN cursos c ON c.id = a.cursoId
        WHERE c.docenteId = :docenteId
        ORDER BY a.asignaturaId, a.creadaEn DESC, a.id DESC
    """)
    fun observarTodasDocente(docenteId: Long): Flow<List<ActividadEntity>>

    @Query("SELECT id FROM actividades WHERE cursoId = :cursoId AND asignaturaId = :asignaturaId AND titulo = :titulo LIMIT 1")
    suspend fun buscarPredeterminada(cursoId: Long, asignaturaId: String, titulo: String): Long?

    // Continúa en el orden del listado y luego retoma las pendientes anteriores.
    @Query("""
        SELECT a.id FROM estudiantes e
        INNER JOIN actividades actual ON actual.id = :actividadId AND actual.cursoId = e.cursoId
        INNER JOIN actividades a ON a.cursoId = actual.cursoId AND a.asignaturaId = actual.asignaturaId
        WHERE e.id = :estudianteId AND a.id != actual.id
        AND NOT EXISTS (
            SELECT 1 FROM intentos_actividad i
            WHERE i.estudianteId = e.id AND i.actividadId = a.id AND i.finalizada = 1
        )
        ORDER BY CASE WHEN a.creadaEn < actual.creadaEn
            OR (a.creadaEn = actual.creadaEn AND a.id < actual.id) THEN 0 ELSE 1 END,
            a.creadaEn DESC, a.id DESC
        LIMIT 1
    """)
    suspend fun buscarSiguientePendiente(estudianteId: Long, actividadId: Long): Long?

    @Query("""
        UPDATE actividades SET pista = :pista WHERE id = :actividadId
        AND cursoId IN (SELECT id FROM cursos WHERE docenteId = :docenteId)
    """)
    suspend fun actualizarPista(actividadId: Long, docenteId: Long, pista: String): Int

    @Query("""
        SELECT a.id, a.asignaturaId, a.titulo
        FROM actividades a INNER JOIN estudiantes e ON e.cursoId = a.cursoId
        WHERE e.id = :estudianteId
        AND NOT EXISTS (SELECT 1 FROM intentos_actividad fin
            WHERE fin.estudianteId = e.id AND fin.actividadId = a.id AND fin.finalizada = 1)
        ORDER BY
            CASE WHEN EXISTS (SELECT 1 FROM intentos_actividad parcial
                WHERE parcial.estudianteId = e.id AND parcial.actividadId = a.id) THEN 0 ELSE 1 END,
            CASE WHEN a.asignaturaId = (
                SELECT ultima.asignaturaId FROM intentos_actividad i
                INNER JOIN actividades ultima ON ultima.id = i.actividadId AND ultima.cursoId = e.cursoId
                WHERE i.estudianteId = e.id ORDER BY i.creadaEn DESC, i.id DESC LIMIT 1
            ) THEN 0 ELSE 1 END,
            a.creadaEn DESC, a.id DESC
        LIMIT 1
    """)
    fun observarParaContinuar(estudianteId: Long): Flow<com.example.mpazpro.model.ActividadParaContinuar?>

    @androidx.room.Update
    suspend fun actualizar(actividad: ActividadEntity): Int

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
          AND NOT EXISTS (SELECT 1 FROM actividades_retiradas r WHERE r.actividadId = a.id)
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
            a.maxIntentos,
            CASE WHEN a.maxIntentos = 3 THEN a.pista ELSE '' END AS pista
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
