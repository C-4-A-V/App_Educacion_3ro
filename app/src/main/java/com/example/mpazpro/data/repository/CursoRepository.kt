package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.CursoDao
import com.example.mpazpro.data.local.CursoEntity
import java.util.Locale
import kotlinx.coroutines.flow.Flow

class CursoRepository(
    private val cursoDao: CursoDao
) {

    fun observarCursos(docenteId: Long): Flow<List<CursoEntity>> {
        return cursoDao.observarPorDocente(docenteId)
    }

    suspend fun crearCurso(
        docenteId: Long,
        nombre: String
    ): Long {
        // Unifica mayúsculas y espacios para detectar duplicados.
        val nombreNormalizado = nombre
            .trim()
            .replace(Regex("\\s+"), " ")
            .uppercase(Locale.ROOT)

        return cursoDao.insertar(
            CursoEntity(
                docenteId = docenteId,
                nombre = nombreNormalizado
            )
        )
    }
    suspend fun obtenerCurso(
        cursoId: Long,
        docenteId: Long
    ): CursoEntity? {
        return cursoDao.buscarPorIdYDocente(
            cursoId = cursoId,
            docenteId = docenteId
        )
    }
}
