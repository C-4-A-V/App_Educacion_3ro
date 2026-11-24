package com.example.mpazpro.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.POST

data class ActividadServidor(
    @SerializedName("id") val id: Long,
    @SerializedName("asignaturaId") val asignatura: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("pregunta") val pregunta: String
)

data class CursoCompartidoServidor(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String
)

data class IdentidadCatalogoServidor(
    @SerializedName("identificador") val identificador: String?,
    @SerializedName("persistente") val persistente: Boolean = false
)

interface ActividadesServidorApi {
    @GET("api/catalogo/identidad")
    suspend fun identidadCatalogo(): IdentidadCatalogoServidor

    @GET("api/cursos-compartidos")
    suspend fun listarCursos(): List<CursoCompartidoServidor>

    @POST("api/cursos-compartidos/siguiente")
    suspend fun crearSiguienteCurso(): CursoCompartidoServidor

    @GET("api/actividades")
    suspend fun listar(): List<ActividadServidor>
}

object ServicioActividadesServidor {
    val api: ActividadesServidorApi by lazy {
        RetrofitServidor.instancia.create(ActividadesServidorApi::class.java)
    }
}
