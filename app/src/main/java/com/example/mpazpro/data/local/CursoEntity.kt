package com.example.mpazpro.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cursos",
    foreignKeys = [
        ForeignKey(
            entity = DocenteEntity::class,
            parentColumns = ["id"],
            childColumns = ["docenteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["docenteId", "nombre"],
            unique = true
        )
    ]
)
data class CursoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val docenteId: Long,
    val nombre: String
)
