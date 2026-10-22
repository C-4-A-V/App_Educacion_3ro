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
}
