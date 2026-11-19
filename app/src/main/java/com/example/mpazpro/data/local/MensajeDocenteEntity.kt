package com.example.mpazpro.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mensajes_docente",
    foreignKeys = [
        ForeignKey(entity = EstudianteEntity::class, parentColumns = ["id"], childColumns = ["estudianteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = DocenteEntity::class, parentColumns = ["id"], childColumns = ["docenteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ActividadEntity::class, parentColumns = ["id"], childColumns = ["actividadId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("estudianteId"), Index("docenteId"), Index("actividadId")]
)
data class MensajeDocenteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val estudianteId: Long,
    val docenteId: Long,
    val actividadId: Long?,
    val contenido: String,
    val creadaEn: Long,
    @ColumnInfo(defaultValue = "0") val leido: Boolean = false
)
