package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mpazpro.model.EstudianteResultadoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface IntentoActividadDao {

    @Query("""
        SELECT DISTINCT a.id FROM actividades a
        JOIN cursos c ON c.id = a.cursoId
        JOIN intentos_actividad i ON i.actividadId = a.id
        WHERE a.cursoId = :cursoId AND a.asignaturaId = :asignaturaId AND c.docenteId = :docenteId
    """)
    fun observarIdsRespondidas(cursoId: Long, asignaturaId: String, docenteId: Long): Flow<List<Long>>

    @Query("""
        SELECT a.id AS actividadId, a.cursoId, e.id AS estudianteId, a.asignaturaId,
            a.titulo, a.pregunta, a.maxIntentos, i.numero AS intentosUsados,
            i.finalizada, i.correcta, i.respuestaElegida, i.respuestaCorrecta,
            EXISTS(SELECT 1 FROM intentos_actividad err
                WHERE err.estudianteId = e.id AND err.actividadId = a.id AND err.correcta = 0) AS tuvoError
        FROM estudiantes e
        JOIN cursos c ON c.id = e.cursoId
        JOIN actividades a ON a.cursoId = c.id
        JOIN intentos_actividad i ON i.estudianteId = e.id AND i.actividadId = a.id
            AND i.numero = (SELECT MAX(ultimo.numero) FROM intentos_actividad ultimo
                WHERE ultimo.estudianteId = e.id AND ultimo.actividadId = a.id)
        WHERE c.docenteId = :docenteId
        ORDER BY a.asignaturaId, a.creadaEn DESC, a.id DESC, e.nombre
    """)
    fun observarDetalleDocente(docenteId: Long): Flow<List<com.example.mpazpro.model.ResultadoPreguntaDocente>>

    @Query("SELECT EXISTS(SELECT 1 FROM intentos_actividad WHERE actividadId = :actividadId)")
    suspend fun tieneIntentos(actividadId: Long): Boolean

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
