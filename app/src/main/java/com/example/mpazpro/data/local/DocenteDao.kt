package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface DocenteDao {

    // Devuelve el ID generado.
    // Si el correo ya existe, devuelve -1 y no reemplaza al docente.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(docente: DocenteEntity): Long

    // Devuelve null si no encuentra ese correo.
    @Query(
        """
        SELECT * FROM docentes
        WHERE LOWER(correo) = LOWER(:correo)
        LIMIT 1
        """
    )
    suspend fun buscarPorCorreo(correo: String): DocenteEntity?
}
