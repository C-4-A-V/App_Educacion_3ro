package com.example.mpazpro.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AsegurarCursoApi {
    @POST("api/cursos-compartidos/{seccion}/asegurar")
    suspend fun asegurar(@Path("seccion") seccion: String, @Header("X-Catalogo-Id") catalogoId: String): CursoCompartidoServidor
}

object ServicioAsegurarCurso {
    val api: AsegurarCursoApi by lazy {
        Retrofit.Builder().baseUrl("http://127.0.0.1:8080/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(AsegurarCursoApi::class.java)
    }
}
