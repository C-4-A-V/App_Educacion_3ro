package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.mpazpro.model.PerfilEstudiante
import kotlinx.coroutines.flow.Flow

@Dao
interface EstudianteDao {

    @Query("UPDATE estudiantes SET fotoRuta = :ruta WHERE id = :estudianteId")
    suspend fun actualizarFotoPerfil(estudianteId: Long, ruta: String): Int

    @Insert
    suspend fun insertar(estudiante: EstudianteEntity): Long

    @Query("SELECT cursoId FROM estudiantes WHERE id = :estudianteId AND retirado = 0 LIMIT 1")
    suspend fun buscarCursoId(estudianteId: Long): Long?

    @Query("SELECT * FROM estudiantes WHERE rut = :rut LIMIT 1")
    suspend fun buscarPorRut(rut: String): EstudianteEntity?

    @Query("SELECT * FROM estudiantes WHERE rut = :rut AND retirado = 0 LIMIT 1")
    suspend fun buscarActivoPorRut(rut: String): EstudianteEntity?

    @Query("""
        UPDATE estudiantes SET retirado = 1
        WHERE id IN (:ids) AND cursoId = :cursoId AND retirado = 0
          AND cursoId IN (SELECT id FROM cursos WHERE docenteId = :docenteId)
    """)
    suspend fun marcarRetirados(ids: List<Long>, cursoId: Long, docenteId: Long): Int

    @Transaction
    suspend fun retirarSeleccion(ids: List<Long>, cursoId: Long, docenteId: Long) {
        require(ids.isNotEmpty() && ids.distinct().size == ids.size)
        check(marcarRetirados(ids, cursoId, docenteId) == ids.size) { "Selección no disponible" }
    }

    @Query("""
        UPDATE estudiantes SET retirado = 0
        WHERE id = :id AND cursoId = :cursoId AND retirado = 1
          AND cursoId IN (SELECT id FROM cursos WHERE docenteId = :docenteId)
    """)
    suspend fun restaurar(id: Long, cursoId: Long, docenteId: Long): Int

    @Query("""
        SELECT e.* FROM estudiantes e JOIN cursos c ON c.id = e.cursoId
        WHERE e.cursoId = :cursoId AND c.docenteId = :docenteId AND e.retirado = 1
        ORDER BY e.nombre COLLATE NOCASE
    """)
    fun observarRetirados(cursoId: Long, docenteId: Long): Flow<List<EstudianteEntity>>

    @Query("""
        UPDATE estudiantes SET rut = :rut, pinHash = :hash, pinSalt = :salt,
            pinIteraciones = :iteraciones, intentosFallidos = 0, bloqueadoHasta = 0
        WHERE id = :estudianteId AND retirado = 0 AND cursoId IN
            (SELECT id FROM cursos WHERE id = :cursoId AND docenteId = :docenteId)
    """)
    suspend fun actualizarAcceso(estudianteId: Long, cursoId: Long, docenteId: Long,
        rut: String, hash: String, salt: String, iteraciones: Int): Int

    @Query("UPDATE estudiantes SET intentosFallidos = :intentos, bloqueadoHasta = :hasta WHERE id = :id")
    suspend fun actualizarIntentos(id: Long, intentos: Int, hasta: Long)


    // Comprueba el curso y el docente al consultar estudiantes.
    @Query(
        """
        SELECT e.* FROM estudiantes AS e
        INNER JOIN cursos AS c ON c.id = e.cursoId
        WHERE e.cursoId = :cursoId
          AND c.docenteId = :docenteId AND e.retirado = 0
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
        WHERE e.retirado = 0
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
            COALESCE(SUM(CASE WHEN i.finalizada = 1 THEN 1 ELSE 0 END), 0) AS completadas,
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
            AND i.numero = (SELECT MAX(ultimo.numero) FROM intentos_actividad ultimo WHERE ultimo.estudianteId = e.id AND ultimo.actividadId = a.id)
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
