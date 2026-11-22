package com.example.mpazpro.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.Query

interface CrearCursoConfirmadoApi {
    @POST("api/cursos-compartidos/siguiente")
    suspend fun crear(@Query("seccionEsperada") seccion: String): CursoCompartidoServidor
}

object ServicioCrearCursoConfirmado {
    val api: CrearCursoConfirmadoApi by lazy {
        Retrofit.Builder().baseUrl("http://127.0.0.1:8080/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(CrearCursoConfirmadoApi::class.java)
    }
}
