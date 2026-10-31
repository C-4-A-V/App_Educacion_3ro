package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mpazpro.model.PerfilEstudiante
import kotlinx.coroutines.flow.Flow

@Dao
interface EstudianteDao {

    @Insert
    suspend fun insertar(estudiante: EstudianteEntity): Long

    // Comprueba el curso y el docente al consultar estudiantes.
    @Query(
        """
        SELECT e.* FROM estudiantes AS e
        INNER JOIN cursos AS c ON c.id = e.cursoId
        WHERE e.cursoId = :cursoId
          AND c.docenteId = :docenteId
        ORDER BY e.nombre COLLATE NOCASE ASC
        """
    )
    fun observarPorCurso(
        cursoId: Long,
        docenteId: Long
    ): Flow<List<EstudianteEntity>>

    @Query(
        """
        SELECT
            e.id,
            e.nombre,
            c.nombre AS cursoNombre,
            e.fotoRuta
        FROM estudiantes AS e
        INNER JOIN cursos AS c ON c.id = e.cursoId
        ORDER BY c.nombre ASC, e.nombre COLLATE NOCASE ASC
        """
    )
    fun observarPerfiles(): Flow<List<PerfilEstudiante>>

    @Query(
        """
        SELECT
            e.id AS estudianteId,
            e.nombre,
            e.fotoRuta,
            e.cursoId,
            a.asignaturaId,
            COUNT(a.id) AS total,
            COUNT(i.id) AS completadas,
            COALESCE(
                SUM(
                    CASE
                        WHEN i.correcta = 1 THEN 1
                        ELSE 0
                    END
                ),
                0
            ) AS correctas
        FROM estudiantes e
        INNER JOIN cursos c ON c.id = e.cursoId
        LEFT JOIN actividades a ON a.cursoId = e.cursoId
        LEFT JOIN intentos_actividad i
            ON i.actividadId = a.id
            AND i.estudianteId = e.id
        WHERE c.docenteId = :docenteId
        GROUP BY
            e.id,
            e.nombre,
            e.fotoRuta,
            e.cursoId,
            a.asignaturaId
        ORDER BY e.nombre COLLATE NOCASE ASC
        """
    )
    fun observarAvancesDocente(
        docenteId: Long
    ): Flow<List<com.example.mpazpro.model.AvanceAlumnoFila>>
}
