package com.example.mpazpro.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Asociación explícita de una cuenta de servidor con un perfil existente del dispositivo. */
@Entity(
    tableName = "identidades_docentes",
    primaryKeys = ["catalogoId", "docenteServidorId"],
    foreignKeys = [ForeignKey(entity = DocenteEntity::class, parentColumns = ["id"],
        childColumns = ["docenteLocalId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["docenteLocalId"], unique = true)]
)
data class IdentidadDocenteEntity(
    val catalogoId: String,
    val docenteServidorId: Long,
    val docenteLocalId: Long
)
