package com.example.mpazpro.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Operación enviada o por enviar: la clave se conserva aunque falle la conexión. */
@Entity(
    tableName = "creaciones_curso_pendientes",
    foreignKeys = [ForeignKey(entity = IdentidadDocenteEntity::class,
        parentColumns = ["catalogoId", "docenteServidorId"],
        childColumns = ["catalogoId", "docenteServidorId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CursoEntity::class, parentColumns = ["id"],
            childColumns = ["cursoLocalId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["catalogoId", "docenteServidorId", "seccion"], unique = true),
        Index(value = ["cursoLocalId"], unique = true)]
)
data class CreacionCursoPendienteEntity(
    @PrimaryKey val clave: String,
    val catalogoId: String,
    val docenteServidorId: Long,
    val seccion: String,
    val cursoLocalId: Long? = null
)
