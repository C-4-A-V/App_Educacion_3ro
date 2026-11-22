package com.example.mpazpro.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vinculos_curso",
    foreignKeys = [ForeignKey(
        entity = CursoEntity::class,
        parentColumns = ["id"],
        childColumns = ["cursoLocalId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["catalogoId", "cursoServidorId"])]
)
data class VinculoCursoEntity(
    @PrimaryKey val cursoLocalId: Long,
    val catalogoId: String,
    val cursoServidorId: Long
)
