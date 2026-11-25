package com.example.mpazpro.data.repository

import androidx.room.withTransaction
import com.example.mpazpro.data.local.IdentidadDocenteEntity
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.remote.ActividadesServidorApi
import java.util.UUID

/** Requiere sesión confirmada y la contraseña de la cuenta local que se desea recuperar. */
class RecuperarDatosDocente(
    private val base: MpazDatabase,
    private val remoto: DocenteRemotoRepository,
    private val catalogo: ActividadesServidorApi
) {
    suspend fun recuperar(correoLocal: String, claveLocal: String): Long {
        val sesion = remoto.recuperarSesion()
        val identidad = catalogo.identidadCatalogo()
        require(identidad.persistente)
        val catalogoId = requireNotNull(identidad.identificador)
        require(UUID.fromString(catalogoId).toString() == catalogoId)
        val local = DocenteRepository(base.docenteDao()).autenticar(correoLocal, claveLocal)
            ?: throw IllegalArgumentException("Credenciales del dispositivo incorrectas")
        check(remoto.recuperarSesion() == sesion) { "La sesión cambió" }
        return base.withTransaction {
            check(remoto.sesion.value == sesion) { "La sesión cambió" }
            val dao = base.identidadDocenteDao()
            val porServidor = dao.buscar(catalogoId, sesion.id)
            val porLocal = dao.buscarPorLocal(local.id)
            check(porServidor == null || porServidor.docenteLocalId == local.id) { "La cuenta ya tiene otra asociación" }
            check(porLocal == null || porLocal == porServidor) { "Los datos ya están asociados a otra cuenta" }
            if (porServidor == null) dao.insertar(IdentidadDocenteEntity(catalogoId, sesion.id, local.id))
            local.id
        }
    }
}
