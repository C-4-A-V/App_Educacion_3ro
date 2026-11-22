package com.example.mpazpro.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/** Conserva la actividad y sus respuestas mientras la oculta del listado activo. */
@Entity(
    tableName = "actividades_retiradas",
    foreignKeys = [ForeignKey(entity = ActividadEntity::class, parentColumns = ["id"],
        childColumns = ["actividadId"], onDelete = ForeignKey.CASCADE)]
)
data class ActividadRetiradaEntity(@PrimaryKey val actividadId: Long)
