package com.example.mpazpro.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "actividades_copiadas",
    primaryKeys = ["origenId", "cursoDestinoId"],
    foreignKeys = [
        ForeignKey(entity = ActividadEntity::class, parentColumns = ["id"], childColumns = ["origenId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CursoEntity::class, parentColumns = ["id"], childColumns = ["cursoDestinoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ActividadEntity::class, parentColumns = ["id"], childColumns = ["copiaId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("cursoDestinoId"), Index("copiaId", unique = true)]
)
data class ActividadCopiaEntity(val origenId: Long, val cursoDestinoId: Long, val copiaId: Long)
