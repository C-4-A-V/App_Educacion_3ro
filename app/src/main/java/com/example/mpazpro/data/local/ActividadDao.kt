package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Insert
    suspend fun insertar(actividad: ActividadEntity): Long

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
}