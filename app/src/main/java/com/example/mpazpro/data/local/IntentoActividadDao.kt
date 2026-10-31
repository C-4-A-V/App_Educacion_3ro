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
        ORDER BY numero DESC
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
            COALESCE(i.finalizada, 0) AS respondida,
            CASE WHEN i.finalizada = 1 THEN i.correcta ELSE NULL END AS correcta,
            i.respuestaElegida AS respuestaElegida,
            COALESCE(i.numero, 0) AS intentosUsados
        FROM estudiantes e
        INNER JOIN actividades a ON a.cursoId = e.cursoId
        INNER JOIN cursos c ON c.id = a.cursoId
        LEFT JOIN intentos_actividad i
            ON i.actividadId = a.id
            AND i.estudianteId = e.id
            AND i.numero = (SELECT MAX(ultimo.numero) FROM intentos_actividad ultimo WHERE ultimo.estudianteId = e.id AND ultimo.actividadId = a.id)
        WHERE a.id = :actividadId
          AND c.docenteId = :docenteId
        ORDER BY e.nombre COLLATE NOCASE ASC
        """
    )
    fun observarResultadosActividad(
        actividadId: Long,
        docenteId: Long
    ): Flow<List<EstudianteResultadoItem>>
}
