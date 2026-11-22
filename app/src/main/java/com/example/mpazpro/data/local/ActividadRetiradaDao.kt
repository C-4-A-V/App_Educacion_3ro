package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ActividadRetiradaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(retiradas: List<ActividadRetiradaEntity>)

    @Query("SELECT EXISTS(SELECT 1 FROM actividades_retiradas WHERE actividadId = :actividadId)")
    suspend fun existe(actividadId: Long): Boolean
}
