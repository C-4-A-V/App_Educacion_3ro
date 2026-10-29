package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mpazpro.model.EstudianteResultadoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface IntentoActividadDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(
        intento: IntentoActividadEntity
    ): Long

    @Query(
        """
        SELECT * FROM intentos_actividad
        WHERE estudianteId = :estudianteId
          AND actividadId = :actividadId
        LIMIT 1
        """
    )
    suspend fun buscar(
        estudianteId: Long,
        actividadId: Long
    ): IntentoActividadEntity?

    @Query(
        """
        SELECT
            e.id AS estudianteId,
            e.nombre AS estudianteNombre,
            e.fotoRuta AS estudianteFoto,
            (i.id IS NOT NULL) AS respondida,
            i.correcta AS correcta,
            i.respuestaElegida AS respuestaElegida
        FROM estudiantes e
        INNER JOIN actividades a ON a.cursoId = e.cursoId
        LEFT JOIN intentos_actividad i ON i.actividadId = a.id AND i.estudianteId = e.id
        WHERE a.id = :actividadId
        ORDER BY e.nombre COLLATE NOCASE ASC
        """
    )
    fun observarResultadosActividad(
        actividadId: Long
    ): Flow<List<EstudianteResultadoItem>>
}
