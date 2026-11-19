package com.example.mpazpro.data.local

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "estudiantes",
    foreignKeys = [
        ForeignKey(
            entity = CursoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cursoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cursoId"]), Index(value = ["rut"], unique = true)]
)
data class EstudianteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cursoId: Long,
    val nombre: String,

    // Más adelante guardaremos la ruta de la fotografía.
    val fotoRuta: String? = null,
    val rut: String? = null,
    val pinHash: String? = null,
    val pinSalt: String? = null,
    val pinIteraciones: Int? = null,
    @ColumnInfo(defaultValue = "0") val intentosFallidos: Int = 0,
    @ColumnInfo(defaultValue = "0") val bloqueadoHasta: Long = 0
)
