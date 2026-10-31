package com.example.mpazpro.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intentos_actividad",
    foreignKeys = [
        ForeignKey(
            entity = EstudianteEntity::class,
            parentColumns = ["id"],
            childColumns = ["estudianteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ActividadEntity::class,
            parentColumns = ["id"],
            childColumns = ["actividadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["estudianteId", "actividadId", "numero"],
            unique = true
        ),
        Index(value = ["actividadId"])
    ]
)
data class IntentoActividadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val estudianteId: Long,
    val actividadId: Long,

    val respuestaElegida: Int,
    val respuestaCorrecta: Int,
    val correcta: Boolean,

    @ColumnInfo(defaultValue = "1")
    val numero: Int = 1,
    @ColumnInfo(defaultValue = "1")
    val finalizada: Boolean = true,

    val creadaEn: Long = System.currentTimeMillis()
)
