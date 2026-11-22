package com.example.mpazpro.data.repository

import androidx.room.withTransaction
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.local.VinculoCursoEntity
import com.example.mpazpro.data.remote.ActividadesServidorApi
import java.util.UUID

/** La selección debe ser explícita; no ejecuta cambios durante la consulta. */
class VinculacionCursoRepository(
    private val database: MpazDatabase,
    private val api: ActividadesServidorApi
) {
    suspend fun vincular(docenteId: Long, cursoLocalId: Long, cursoServidorId: Long) {
        require(cursoServidorId > 0)
        val inicial = api.identidadCatalogo()
        check(inicial.persistente) { "El catálogo es temporal" }
        val catalogo = validarIdentidad(inicial.identificador)
        val cursos = api.listarCursos()
        val seleccionado = cursos.filter { it.id == cursoServidorId }.singleOrNull()
            ?: throw IllegalArgumentException("Curso compartido no disponible")
        val posterior = api.identidadCatalogo()
        check(posterior.persistente && catalogo == validarIdentidad(posterior.identificador)) {
            "El catálogo cambió durante la consulta"
        }

        database.withTransaction {
            val local = database.cursoDao().buscarPorIdYDocente(cursoLocalId, docenteId)
                ?: throw IllegalArgumentException("Curso local no disponible")
            val seccionLocal = PropuestaVinculacionCursos.seccion(local.nombre)
            require(seccionLocal != null && seccionLocal == PropuestaVinculacionCursos.seccion(seleccionado.nombre)) {
                "Los cursos seleccionados no corresponden a la misma sección"
            }
            val anterior = database.vinculoCursoDao().buscar(cursoLocalId)
            val nuevo = VinculoCursoEntity(cursoLocalId, catalogo, cursoServidorId)
            check(anterior == null || anterior == nuevo) { "El curso ya tiene otro vínculo" }
            if (anterior == null) database.vinculoCursoDao().insertar(nuevo)
        }
    }

    private fun validarIdentidad(valor: String?): String {
        val texto = requireNotNull(valor)
        val uuid = UUID.fromString(texto)
        require(uuid.toString() == texto) { "Identidad de catálogo inválida" }
        return texto
    }
}
