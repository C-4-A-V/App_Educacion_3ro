package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.ActividadDao
import com.example.mpazpro.data.local.CursoDao
import com.example.mpazpro.data.local.EstudianteDao
import com.example.mpazpro.data.local.EstudianteEntity

class EstudianteRepository(
    private val estudianteDao: EstudianteDao,
    private val cursoDao: CursoDao,
    private val actividadDao: ActividadDao
) {

    suspend fun obtenerCurso(
        cursoId: Long,
        docenteId: Long
    ) = cursoDao.buscarPorIdYDocente(cursoId, docenteId)

    fun observarEstudiantes(
        cursoId: Long,
        docenteId: Long
    ) = estudianteDao.observarPorCurso(cursoId, docenteId)

    fun observarPerfiles() = estudianteDao.observarPerfiles()

    fun observarProgreso(estudianteId: Long) =
        actividadDao.observarProgresoEstudiante(estudianteId)

    suspend fun registrar(
        cursoId: Long,
        docenteId: Long,
        nombre: String,
        fotoRuta: String
    ): Long {
        // No basta con conocer el ID del curso para modificarlo.
        check(obtenerCurso(cursoId, docenteId) != null) {
            "Curso no disponible para este docente"
        }

        val nombreLimpio = nombre
            .trim()
            .replace(Regex("\\s+"), " ")

        require(nombreLimpio.length >= 2)

        return estudianteDao.insertar(
            EstudianteEntity(
                cursoId = cursoId,
                nombre = nombreLimpio,
                fotoRuta = fotoRuta
            )
        )
    }
}
