package com.example.mpazpro.data.remote

import com.example.mpazpro.BuildConfig
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

data class CredencialesDocenteRemoto(val correo: String, val clave: String) {
    override fun toString(): String = "CredencialesDocenteRemoto([oculto])"
}
data class PerfilDocenteRemoto(val id: Long, val correo: String)
data class CursoDocenteRemoto(val id: Long, val nombre: String)
data class CrearCursoDocenteRemoto(val seccion: String)
data class ResultadoAgregadoRemoto(val actividadId: Long, val finalizadas: Long, val correctas: Long)

interface DocenteAutenticadoApi {
    @POST("api/docentes/sesion/registro")
    suspend fun registrar(@Body datos: CredencialesDocenteRemoto): Response<PerfilDocenteRemoto>
    @POST("api/docentes/sesion/login")
    suspend fun entrar(@Body datos: CredencialesDocenteRemoto): Response<PerfilDocenteRemoto>
    @GET("api/docentes/sesion")
    suspend fun sesion(): Response<PerfilDocenteRemoto>
    @POST("api/docentes/sesion/logout")
    suspend fun salir(): Response<Unit>
    @GET("api/docentes/cursos")
    suspend fun cursos(): Response<List<CursoDocenteRemoto>>
    @POST("api/docentes/cursos")
    suspend fun crearCurso(@Body datos: CrearCursoDocenteRemoto): Response<CursoDocenteRemoto>
    @POST("api/docentes/cursos/{cursoId}/publicaciones")
    suspend fun publicar(@Path("cursoId") cursoId: Long, @Header("Idempotency-Key") clave: String,
                         @Body actividad: SolicitudPublicacion): Response<RespuestaPublicacion>
    @POST("api/docentes/cursos/{cursoId}/publicaciones/retirar")
    suspend fun retirar(@Path("cursoId") cursoId: Long, @Body claves: List<String>): Response<Unit>
    @GET("api/docentes/cursos/{cursoId}/actividades/{actividadId}/resultados")
    suspend fun resultados(@Path("cursoId") cursoId: Long,
                           @Path("actividadId") actividadId: Long): Response<ResultadoAgregadoRemoto>
}

/** Sesión solo en memoria; nunca persiste credenciales ni cookies. */
class ClienteDocenteAutenticado(baseUrl: String) {
    private val url = baseUrl.toHttpUrl()
    private val cookies = CookiesDocente()
    init {
        require(url.isHttps || (BuildConfig.DEBUG && url.host == "127.0.0.1")) {
            "El servidor debe usar HTTPS; HTTP solo se permite para pruebas locales."
        }
    }
    private val http = OkHttpClient.Builder().cookieJar(cookies)
        .retryOnConnectionFailure(false)
        .addInterceptor { cadena ->
            val respuesta = cadena.proceed(cadena.request())
            if (respuesta.code == 401) cookies.limpiar()
            respuesta
        }.build()
    val api: DocenteAutenticadoApi = Retrofit.Builder().baseUrl(url).client(http)
        .addConverterFactory(GsonConverterFactory.create()).build().create(DocenteAutenticadoApi::class.java)

    fun limpiarSesion() = cookies.limpiar()
}

private class CookiesDocente : CookieJar {
    private val almacen = mutableListOf<Cookie>()
    @Synchronized override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val ahora = System.currentTimeMillis()
        almacen.removeAll { anterior ->
            anterior.expiresAt <= ahora || cookies.any {
                it.name == anterior.name && it.domain == anterior.domain && it.path == anterior.path
            }
        }
        almacen.addAll(cookies.filter { it.expiresAt > ahora })
    }
    @Synchronized override fun loadForRequest(url: HttpUrl): List<Cookie> {
        almacen.removeAll { it.expiresAt <= System.currentTimeMillis() }
        return almacen.filter { it.matches(url) }
    }
    @Synchronized fun limpiar() = almacen.clear()
}
