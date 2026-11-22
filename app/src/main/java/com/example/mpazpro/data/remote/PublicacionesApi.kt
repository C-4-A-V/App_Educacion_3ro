package com.example.mpazpro.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class SolicitudPublicacion(
    @SerializedName("cursoId") val cursoId: Long,
    @SerializedName("asignaturaId") val asignaturaId: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("instrucciones") val instrucciones: String,
    @SerializedName("pregunta") val pregunta: String,
    @SerializedName("alternativaA") val alternativaA: String,
    @SerializedName("alternativaB") val alternativaB: String,
    @SerializedName("alternativaC") val alternativaC: String,
    @SerializedName("alternativaD") val alternativaD: String,
    @SerializedName("respuestaCorrecta") val respuestaCorrecta: Int,
    @SerializedName("maxIntentos") val maxIntentos: Int,
    @SerializedName("explicacion") val explicacion: String,
    @SerializedName("pista") val pista: String
)

data class RespuestaPublicacion(
    @SerializedName("actividadId") val actividadId: Long?,
    @SerializedName("creada") val creada: Boolean
)

interface PublicacionesApi {
    @POST("api/publicaciones")
    suspend fun publicar(
        @Header("Idempotency-Key") clave: String,
        @Header("X-Catalogo-Id") catalogoId: String,
        @Body solicitud: SolicitudPublicacion
    ): RespuestaPublicacion
}

object ServicioPublicaciones {
    val api: PublicacionesApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://127.0.0.1:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PublicacionesApi::class.java)
    }
}
