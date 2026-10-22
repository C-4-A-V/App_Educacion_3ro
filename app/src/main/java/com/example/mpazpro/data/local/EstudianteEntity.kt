package com.example.mpazpro.data.local

import androidx.room.Entity
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
    indices = [Index(value = ["cursoId"])]
)
data class EstudianteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cursoId: Long,
    val nombre: String,

    // Más adelante guardaremos la ruta de la fotografía.
    val fotoRuta: String? = null
)
