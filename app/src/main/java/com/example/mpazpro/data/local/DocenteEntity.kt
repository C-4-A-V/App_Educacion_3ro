package com.example.mpazpro.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "docentes",
    indices = [Index(value = ["correo"], unique = true)]
)
data class DocenteEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nombre: String,

    val correo: String,

    // No guardaremos la contraseña original.
    val claveHash: String,

    // Valor aleatorio utilizado al calcular el hash.
    val claveSalt: String,

    // Parámetro del algoritmo que definiremos al crear el registro.
    val claveIteraciones: Int,

    val fotoRuta: String? = null
) {
    /** Los tres valores vacíos representan ausencia de credencial local, no una contraseña. */
    fun tieneCredencialLocal(): Boolean =
        claveHash.isNotEmpty() && claveSalt.isNotEmpty() && claveIteraciones > 0

    companion object {
        fun soporteRemoto(correo: String) = DocenteEntity(
            nombre = correo, correo = correo, claveHash = "", claveSalt = "", claveIteraciones = 0
        )
    }
}
