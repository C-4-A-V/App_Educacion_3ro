package com.example.mpazpro.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header
import retrofit2.http.Query

data class ActividadRemotaEstudiante(
    @SerializedName("id") val id: Long,
    @SerializedName("asignaturaId") val asignaturaId: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("instrucciones") val instrucciones: String,
    @SerializedName("pregunta") val pregunta: String,
    @SerializedName("alternativaA") val alternativaA: String,
    @SerializedName("alternativaB") val alternativaB: String,
    @SerializedName("alternativaC") val alternativaC: String,
    @SerializedName("alternativaD") val alternativaD: String,
    @SerializedName("maxIntentos") val maxIntentos: Int,
    @SerializedName("pista") val pista: String
)

data class SolicitudRespuestaAula(
    @SerializedName("actividadId") val actividadId: Long,
    @SerializedName("cursoId") val cursoId: Long,
    @SerializedName("estudianteToken") val estudianteToken: String,
    @SerializedName("numero") val numero: Int,
    @SerializedName("respuestaElegida") val respuestaElegida: Int
)

data class SolicitudEstadoRespuestaAula(
    @SerializedName("actividadId") val actividadId: Long,
    @SerializedName("cursoId") val cursoId: Long,
    @SerializedName("estudianteToken") val estudianteToken: String
)

data class ResultadoRespuestaAula(
    @SerializedName("numero") val numero: Int,
    @SerializedName("respuestaElegida") val respuestaElegida: Int,
    @SerializedName("correcta") val correcta: Boolean,
    @SerializedName("finalizada") val finalizada: Boolean,
    @SerializedName("respuestaCorrecta") val respuestaCorrecta: Int?,
    @SerializedName("explicacion") val explicacion: String?
)

object ServicioAulaActividades {
    val api: AulaActividadesApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://127.0.0.1:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AulaActividadesApi::class.java)
    }
}

interface AulaActividadesApi {
    @POST("api/aula/respuestas")
    suspend fun responder(
        @Header("X-Catalogo-Id") catalogoId: String,
        @Body solicitud: SolicitudRespuestaAula
    ): ResultadoRespuestaAula

    @POST("api/aula/respuestas/estado")
    suspend fun estado(
        @Header("X-Catalogo-Id") catalogoId: String,
        @Body solicitud: SolicitudEstadoRespuestaAula
    ): Response<ResultadoRespuestaAula>

    @GET("api/aula/actividades/{id}")
    suspend fun obtener(
        @Header("X-Catalogo-Id") catalogoId: String,
        @Path("id") actividadId: Long,
        @Query("cursoId") cursoId: Long
    ): ActividadRemotaEstudiante

    @GET("api/aula/actividades")
    suspend fun listar(
        @Header("X-Catalogo-Id") catalogoId: String,
        @Query("cursoId") cursoId: Long,
        @Query("asignaturaId") asignaturaId: String
    ): List<ActividadRemotaEstudiante>
}
