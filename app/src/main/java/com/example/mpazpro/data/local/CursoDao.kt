package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mpazpro.data.repository.PropuestaVinculacionCursos
import kotlinx.coroutines.flow.Flow

@Dao
interface CursoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(curso: CursoEntity): Long

    @Query("SELECT * FROM cursos WHERE docenteId = :docenteId ORDER BY id")
    suspend fun listarPorDocente(docenteId: Long): List<CursoEntity>

    @Transaction
    suspend fun crearSiguienteConfirmado(docenteId: Long, seccionConfirmada: Char): Long {
        val siguiente = PropuestaVinculacionCursos.siguiente(listarPorDocente(docenteId).map { it.nombre })
        check(siguiente != null && siguiente == seccionConfirmada) { "La lista de cursos cambió o necesita revisión" }
        return insertar(CursoEntity(docenteId = docenteId, nombre = "3.º Básico $siguiente")).also {
            check(it > 0) { "El curso ya existe" }
        }
    }

    @Query("SELECT * FROM cursos WHERE docenteId = :docenteId ORDER BY id LIMIT 1")
    suspend fun buscarPrimero(docenteId: Long): CursoEntity?

    // Solo devuelve los cursos del docente indicado.
    // Flow actualiza la lista cuando cambia la tabla.
    @Query(
        """
        SELECT * FROM cursos
        WHERE docenteId = :docenteId
        ORDER BY nombre ASC
        """
    )
    fun observarPorDocente(docenteId: Long): Flow<List<CursoEntity>>

    @Query(
        """
        SELECT * FROM cursos
        WHERE id = :cursoId AND docenteId = :docenteId
        LIMIT 1
        """
    )
    suspend fun buscarPorIdYDocente(
        cursoId: Long,
        docenteId: Long
    ): CursoEntity?
}
