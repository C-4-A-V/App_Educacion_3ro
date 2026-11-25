package com.example.mpazpro.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Solo asociaciones confirmadas para una identidad autenticada; no modifica vínculos antiguos. */
@Entity(
    tableName = "vinculos_curso_autenticado",
    primaryKeys = ["catalogoId", "docenteServidorId", "cursoServidorId"],
    foreignKeys = [
        ForeignKey(entity = IdentidadDocenteEntity::class,
            parentColumns = ["catalogoId", "docenteServidorId"],
            childColumns = ["catalogoId", "docenteServidorId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CursoEntity::class, parentColumns = ["id"],
            childColumns = ["cursoLocalId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["cursoLocalId"], unique = true)]
)
data class VinculoCursoAutenticadoEntity(
    val catalogoId: String,
    val docenteServidorId: Long,
    val cursoServidorId: Long,
    val cursoLocalId: Long
)
