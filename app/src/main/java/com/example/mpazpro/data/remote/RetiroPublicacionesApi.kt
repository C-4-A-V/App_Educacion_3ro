package com.example.mpazpro.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class SolicitudRetiro(val cursoId: Long, val claves: List<String>)

interface RetiroPublicacionesApi {
    @POST("api/publicaciones/retirar")
    suspend fun retirar(@Header("X-Catalogo-Id") catalogoId: String, @Body solicitud: SolicitudRetiro)
}

object ServicioRetiroPublicaciones {
    val api: RetiroPublicacionesApi by lazy {
        RetrofitServidor.instancia.create(RetiroPublicacionesApi::class.java)
    }
}
