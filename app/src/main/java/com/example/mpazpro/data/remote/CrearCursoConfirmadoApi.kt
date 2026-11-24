package com.example.mpazpro.data.remote

import retrofit2.http.POST
import retrofit2.http.Query

interface CrearCursoConfirmadoApi {
    @POST("api/cursos-compartidos/siguiente")
    suspend fun crear(@Query("seccionEsperada") seccion: String): CursoCompartidoServidor
}

object ServicioCrearCursoConfirmado {
    val api: CrearCursoConfirmadoApi by lazy {
        RetrofitServidor.instancia.create(CrearCursoConfirmadoApi::class.java)
    }
}
