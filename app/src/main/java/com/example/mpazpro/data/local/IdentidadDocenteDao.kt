package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IdentidadDocenteDao {
    @Insert
    suspend fun insertar(identidad: IdentidadDocenteEntity)

    @Query("SELECT * FROM identidades_docentes WHERE catalogoId = :catalogo AND docenteServidorId = :servidorId")
    suspend fun buscar(catalogo: String, servidorId: Long): IdentidadDocenteEntity?

    @Query("SELECT * FROM identidades_docentes WHERE docenteLocalId = :localId")
    suspend fun buscarPorLocal(localId: Long): IdentidadDocenteEntity?
}
