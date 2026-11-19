package com.example.mpazpro.data.remote

import com.google.gson.JsonArray
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface PaisesApi {
    @GET("v2/country/CHL?format=json")
    suspend fun obtenerChile(): JsonArray
}

object ServicioPaises {
    val api: PaisesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.worldbank.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaisesApi::class.java)
    }
}
