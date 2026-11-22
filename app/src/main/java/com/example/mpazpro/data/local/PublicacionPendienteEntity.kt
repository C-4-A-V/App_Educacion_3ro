package com.example.mpazpro.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "publicaciones_pendientes",
    foreignKeys = [ForeignKey(
        entity = ActividadEntity::class,
        parentColumns = ["id"],
        childColumns = ["actividadLocalId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["clavePublicacion"], unique = true)]
)
data class PublicacionPendienteEntity(
    @PrimaryKey val actividadLocalId: Long,
    val clavePublicacion: String,
    val catalogoId: String,
    val cursoServidorId: Long,
    val actividadServidorId: Long? = null
)
