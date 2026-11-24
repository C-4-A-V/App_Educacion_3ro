package com.example.mpazpro.data.remote

import com.example.mpazpro.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Cliente compartido para las API del aula; la dirección depende de la variante. */
internal object RetrofitServidor {
    val instancia: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.URL_SERVIDOR)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
