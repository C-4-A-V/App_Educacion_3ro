package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

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
}
