package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VinculoCursoDao {
    @Insert
    suspend fun insertar(vinculo: VinculoCursoEntity)

    @Query("""
        SELECT v.* FROM vinculos_curso v JOIN cursos c ON c.id = v.cursoLocalId
        WHERE c.docenteId = :docenteId
    """)
    fun observarPorDocente(docenteId: Long): Flow<List<VinculoCursoEntity>>

    @Query("""
        SELECT v.* FROM vinculos_curso v JOIN cursos c ON c.id = v.cursoLocalId
        WHERE c.docenteId = :docenteId
    """)
    suspend fun listarPorDocente(docenteId: Long): List<VinculoCursoEntity>

    @Query("SELECT * FROM vinculos_curso WHERE cursoLocalId = :cursoLocalId")
    fun observar(cursoLocalId: Long): Flow<VinculoCursoEntity?>

    @Query("SELECT * FROM vinculos_curso WHERE cursoLocalId = :cursoLocalId")
    suspend fun buscar(cursoLocalId: Long): VinculoCursoEntity?
}
