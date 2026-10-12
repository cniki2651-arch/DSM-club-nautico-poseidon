package com.dsm.clubnauticoposeidon.data.remote



import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.open-meteo.com/"
    private const val MARINE_BASE_URL = "https://marine-api.open-meteo.com/"

    val climaApi: ClimaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApi::class.java)
    }

    val marineApi: MarineApi by lazy {
        Retrofit.Builder()
            .baseUrl(MARINE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarineApi::class.java)
    }
}