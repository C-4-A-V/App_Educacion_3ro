package com.example.mpazpro.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class AccesoEstudiantePrueba(val rut: String, val pin: String)
data class EstadoSesionPrueba(val autenticado: Boolean)
data class ProgresoSesionPrueba(val total: Int, val completadas: Int, val correctas: Int)
data class RespuestaSesionPrueba(
    @SerializedName("actividadId") val actividadId: Long,
    @SerializedName("numero") val numero: Int,
    @SerializedName("respuestaElegida") val respuestaElegida: Int
)

interface SesionEstudiantePruebaApi {
    @POST("api/sesion-estudiante-prueba/login")
    suspend fun entrar(@Body datos: AccesoEstudiantePrueba): EstadoSesionPrueba

    @GET("api/sesion-estudiante-prueba")
    suspend fun estado(): Response<EstadoSesionPrueba>

    @POST("api/sesion-estudiante-prueba/logout")
    suspend fun salir(): Response<Unit>

    @GET("api/sesion-estudiante-prueba/actividades")
    suspend fun actividades(@Query("asignaturaId") asignatura: String): List<ActividadRemotaEstudiante>

    @GET("api/sesion-estudiante-prueba/actividades/progreso")
    suspend fun progreso(@Query("asignaturaId") asignatura: String? = null): ProgresoSesionPrueba

    @GET("api/sesion-estudiante-prueba/actividades/{id}")
    suspend fun actividad(@Path("id") id: Long): ActividadRemotaEstudiante

    @POST("api/sesion-estudiante-prueba/respuestas")
    suspend fun responder(@Body datos: RespuestaSesionPrueba): ResultadoRespuestaAula

    @GET("api/sesion-estudiante-prueba/respuestas/{id}")
    suspend fun resultado(@Path("id") id: Long): Response<ResultadoRespuestaAula>
}

/** Solo para pruebas locales. No persiste cookies ni habilita el aula en release. */
class ClienteSesionEstudiantePrueba(baseUrl: String) {
    private val jar = CookiesEnMemoria()
    val api: SesionEstudiantePruebaApi = Retrofit.Builder().baseUrl(baseUrl)
        .client(OkHttpClient.Builder().cookieJar(jar).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(SesionEstudiantePruebaApi::class.java)

    fun limpiarSesion() = jar.limpiar()
}

private class CookiesEnMemoria : CookieJar {
    private val cookies = mutableListOf<Cookie>()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, nuevas: List<Cookie>) {
        val ahora = System.currentTimeMillis()
        cookies.removeAll { anterior ->
            anterior.expiresAt <= ahora || nuevas.any {
                it.name == anterior.name && it.domain == anterior.domain && it.path == anterior.path
            }
        }
        cookies.addAll(nuevas.filter { it.expiresAt > ahora })
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        cookies.removeAll { it.expiresAt <= System.currentTimeMillis() }
        return cookies.filter { it.matches(url) }
    }

    @Synchronized
    fun limpiar() = cookies.clear()
}
