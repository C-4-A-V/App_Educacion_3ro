package com.example.mpazpro.data.repository

import androidx.room.withTransaction
import com.example.mpazpro.data.local.DocenteEntity
import com.example.mpazpro.data.local.IdentidadDocenteEntity
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.remote.ActividadesServidorApi
import com.example.mpazpro.model.DocenteSesion
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.first

class RecuperacionDocenteNecesaria : Exception()

/** Resuelve el perfil de Room únicamente a partir de una sesión y un catálogo verificados. */
class ResolverIdentidadDocente(
    private val base: MpazDatabase,
    private val remoto: DocenteRemotoRepository,
    private val catalogo: ActividadesServidorApi
) {
    suspend fun resolver(): DocenteSesion {
        val sesion = remoto.recuperarSesion()
        val identidad = catalogo.identidadCatalogo()
        check(identidad.persistente) { "El catálogo no tiene una identidad persistente" }
        val catalogoId = requireNotNull(identidad.identificador)
        check(UUID.fromString(catalogoId).toString() == catalogoId)
        check(remoto.recuperarSesion() == sesion) { "La sesión cambió" }
        return base.withTransaction {
            check(remoto.sesion.value == sesion) { "La sesión cambió" }
            val identidadDao = base.identidadDocenteDao()
            val docenteDao = base.docenteDao()
            val vinculada = identidadDao.buscar(catalogoId, sesion.id)
            val local = if (vinculada != null) {
                requireNotNull(docenteDao.observarPorId(vinculada.docenteLocalId).first())
            } else {
                val correo = sesion.correo.trim().lowercase(Locale.ROOT)
                if (docenteDao.buscarPorCorreo(correo) != null) throw RecuperacionDocenteNecesaria()
                val id = docenteDao.insertar(DocenteEntity.soporteRemoto(correo))
                check(id > 0) { "No se pudo crear el perfil de apoyo" }
                identidadDao.insertar(IdentidadDocenteEntity(catalogoId, sesion.id, id))
                requireNotNull(docenteDao.observarPorId(id).first())
            }
            DocenteSesion(local.id, local.nombre, local.correo)
        }
    }
}
