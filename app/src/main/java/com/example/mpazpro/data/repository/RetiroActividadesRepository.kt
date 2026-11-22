package com.example.mpazpro.data.repository

import androidx.room.withTransaction
import com.example.mpazpro.data.local.ActividadRetiradaEntity
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.remote.ActividadesServidorApi
import com.example.mpazpro.data.remote.RetiroPublicacionesApi
import com.example.mpazpro.data.remote.SolicitudRetiro

/** Retira en el servidor antes de ocultar las copias locales; nunca elimina intentos. */
class RetiroActividadesRepository(
    private val database: MpazDatabase,
    private val catalogo: ActividadesServidorApi,
    private val retiro: RetiroPublicacionesApi
) {
    suspend fun retirar(cursoId: Long, asignaturaId: String, docenteId: Long, ids: List<Long>) {
        require(ids.isNotEmpty() && ids.size <= 200 && ids.distinct().size == ids.size)
        val claves = database.withTransaction {
            requireNotNull(database.cursoDao().buscarPorIdYDocente(cursoId, docenteId))
            ids.mapNotNull { id ->
                val actividad = requireNotNull(database.actividadDao().buscarPorIdYDocente(id, docenteId))
                require(actividad.cursoId == cursoId && actividad.asignaturaId == asignaturaId)
                check(!database.actividadRetiradaDao().existe(id))
                database.publicacionPendienteDao().buscar(id)
            }
        }
        if (claves.isNotEmpty()) {
            val vinculo = requireNotNull(database.vinculoCursoDao().buscar(cursoId))
            check(claves.all { it.catalogoId == vinculo.catalogoId && it.cursoServidorId == vinculo.cursoServidorId })
            val identidad = catalogo.identidadCatalogo()
            check(identidad.persistente && identidad.identificador == vinculo.catalogoId)
            retiro.retirar(vinculo.catalogoId, SolicitudRetiro(vinculo.cursoServidorId, claves.map { it.clavePublicacion }))
        }
        database.withTransaction {
            requireNotNull(database.cursoDao().buscarPorIdYDocente(cursoId, docenteId))
            ids.forEach { id ->
                val actividad = requireNotNull(database.actividadDao().buscarPorIdYDocente(id, docenteId))
                require(actividad.cursoId == cursoId && actividad.asignaturaId == asignaturaId)
            }
            database.actividadRetiradaDao().insertar(ids.map(::ActividadRetiradaEntity))
        }
    }
}
