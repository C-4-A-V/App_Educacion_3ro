package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CursoAutenticadoDao {
    @Insert
    suspend fun insertarVinculo(vinculo: VinculoCursoAutenticadoEntity)

    @Query("SELECT * FROM vinculos_curso_autenticado WHERE catalogoId = :catalogo AND docenteServidorId = :docente AND cursoServidorId = :curso")
    suspend fun buscarVinculo(catalogo: String, docente: Long, curso: Long): VinculoCursoAutenticadoEntity?

    @Query("SELECT cursoLocalId FROM vinculos_curso_autenticado WHERE catalogoId = :catalogo AND docenteServidorId = :docente")
    suspend fun localesVinculados(catalogo: String, docente: Long): List<Long>

    @Query("SELECT * FROM vinculos_curso_autenticado WHERE cursoLocalId = :curso")
    suspend fun buscarPorLocal(curso: Long): VinculoCursoAutenticadoEntity?

    @Insert
    suspend fun insertarPendiente(pendiente: CreacionCursoPendienteEntity)

    @Query("SELECT * FROM creaciones_curso_pendientes WHERE catalogoId = :catalogo AND docenteServidorId = :docente AND seccion = :seccion")
    suspend fun buscarPendiente(catalogo: String, docente: Long, seccion: String): CreacionCursoPendienteEntity?

    @Query("SELECT * FROM creaciones_curso_pendientes WHERE catalogoId = :catalogo AND docenteServidorId = :docente ORDER BY rowid")
    suspend fun pendientes(catalogo: String, docente: Long): List<CreacionCursoPendienteEntity>

    @Query("DELETE FROM creaciones_curso_pendientes WHERE clave = :clave")
    suspend fun eliminarPendiente(clave: String)
}
