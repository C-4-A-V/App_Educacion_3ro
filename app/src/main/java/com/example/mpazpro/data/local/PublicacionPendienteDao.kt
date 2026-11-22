package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PublicacionPendienteDao {
    @Insert
    suspend fun insertar(publicacion: PublicacionPendienteEntity)

    @Query("SELECT * FROM publicaciones_pendientes WHERE actividadLocalId = :actividadId")
    suspend fun buscar(actividadId: Long): PublicacionPendienteEntity?

    @Query("""
        SELECT p.* FROM publicaciones_pendientes p
        JOIN actividades a ON a.id = p.actividadLocalId
        JOIN cursos c ON c.id = a.cursoId
        WHERE a.cursoId = :cursoId AND a.asignaturaId = :asignaturaId AND c.docenteId = :docenteId
    """)
    fun observarPorAsignatura(cursoId: Long, asignaturaId: String, docenteId: Long): Flow<List<PublicacionPendienteEntity>>

    @Query("SELECT * FROM publicaciones_pendientes p WHERE p.actividadServidorId IS NULL AND NOT EXISTS (SELECT 1 FROM actividades_retiradas r WHERE r.actividadId = p.actividadLocalId) ORDER BY p.actividadLocalId")
    suspend fun pendientes(): List<PublicacionPendienteEntity>

    @Query("""
        SELECT p.* FROM publicaciones_pendientes p
        JOIN actividades a ON a.id = p.actividadLocalId
        JOIN cursos c ON c.id = a.cursoId
        WHERE c.docenteId = :docenteId AND p.actividadServidorId IS NULL
          AND NOT EXISTS (SELECT 1 FROM actividades_retiradas r WHERE r.actividadId = p.actividadLocalId)
        ORDER BY p.actividadLocalId
    """)
    suspend fun pendientesPorDocente(docenteId: Long): List<PublicacionPendienteEntity>

    @Query("""
        SELECT DISTINCT p.actividadServidorId FROM publicaciones_pendientes p
        JOIN actividades origen ON origen.id = p.actividadLocalId
        WHERE p.catalogoId = :catalogoId
          AND p.cursoServidorId = :cursoServidorId
          AND p.actividadServidorId IS NOT NULL
          AND EXISTS (
            SELECT 1 FROM actividades local
            WHERE local.cursoId = :cursoLocalId
              AND local.asignaturaId = origen.asignaturaId
              AND local.titulo = origen.titulo
              AND local.instrucciones = origen.instrucciones
              AND local.pregunta = origen.pregunta
              AND local.alternativaA = origen.alternativaA
              AND local.alternativaB = origen.alternativaB
              AND local.alternativaC = origen.alternativaC
              AND local.alternativaD = origen.alternativaD
              AND local.respuestaCorrecta = origen.respuestaCorrecta
              AND local.maxIntentos = origen.maxIntentos
              AND local.explicacion = origen.explicacion
              AND local.pista = origen.pista
          )
    """)
    suspend fun idsConCopiaLocal(cursoLocalId: Long, catalogoId: String, cursoServidorId: Long): List<Long>

    @Query("SELECT * FROM publicaciones_pendientes WHERE catalogoId = :catalogoId AND cursoServidorId = :cursoServidorId AND actividadServidorId = :servidorId LIMIT 1")
    suspend fun buscarPorServidor(catalogoId: String, cursoServidorId: Long, servidorId: Long): PublicacionPendienteEntity?

    @Query("UPDATE publicaciones_pendientes SET actividadServidorId = :servidorId WHERE actividadLocalId = :actividadId AND actividadServidorId IS NULL")
    suspend fun marcarPublicada(actividadId: Long, servidorId: Long): Int
}
