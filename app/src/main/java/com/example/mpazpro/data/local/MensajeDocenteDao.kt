package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Query
import com.example.mpazpro.model.MensajeEstudiante
import kotlinx.coroutines.flow.Flow

@Dao
interface MensajeDocenteDao {
    @Query("""
        INSERT INTO mensajes_docente (estudianteId, docenteId, actividadId, contenido, creadaEn, leido)
        SELECT e.id, c.docenteId, a.id, :contenido, :creadaEn, 0
        FROM estudiantes e
        JOIN cursos c ON c.id = e.cursoId
        JOIN actividades a ON a.cursoId = c.id
        WHERE e.id = :estudianteId AND a.id = :actividadId AND c.docenteId = :docenteId
    """)
    suspend fun enviar(docenteId: Long, estudianteId: Long, actividadId: Long, contenido: String, creadaEn: Long): Long

    @Query("""
        SELECT m.id, m.contenido, m.creadaEn, m.leido, d.nombre AS docenteNombre,
            a.titulo AS actividadTitulo
        FROM mensajes_docente m
        JOIN docentes d ON d.id = m.docenteId
        LEFT JOIN actividades a ON a.id = m.actividadId
        WHERE m.estudianteId = :estudianteId
        ORDER BY m.creadaEn DESC, m.id DESC
    """)
    fun observar(estudianteId: Long): Flow<List<MensajeEstudiante>>

    @Query("UPDATE mensajes_docente SET leido = 1 WHERE id = :id AND estudianteId = :estudianteId")
    suspend fun marcarLeido(id: Long, estudianteId: Long): Int
}
