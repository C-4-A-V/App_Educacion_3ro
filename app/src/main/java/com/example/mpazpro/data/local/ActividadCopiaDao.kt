package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ActividadCopiaDao {
    @Query("SELECT * FROM actividades_copiadas WHERE origenId = :origenId AND cursoDestinoId = :cursoId")
    suspend fun buscar(origenId: Long, cursoId: Long): ActividadCopiaEntity?

    @Insert
    suspend fun insertar(copia: ActividadCopiaEntity)
}
