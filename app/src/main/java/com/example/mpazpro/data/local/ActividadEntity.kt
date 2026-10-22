package com.example.mpazpro.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "actividades",
    foreignKeys = [
        ForeignKey(
            entity = CursoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cursoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cursoId", "asignaturaId"])
    ]
)
data class ActividadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val cursoId: Long,
    val asignaturaId: String,

    val titulo: String,
    val instrucciones: String,
    val pregunta: String,

    val alternativaA: String,
    val alternativaB: String,
    val alternativaC: String,
    val alternativaD: String,

    // 0 = A, 1 = B, 2 = C, 3 = D.
    val respuestaCorrecta: Int,

    val creadaEn: Long = System.currentTimeMillis()
)