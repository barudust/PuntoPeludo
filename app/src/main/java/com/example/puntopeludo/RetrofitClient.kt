package com.example.puntopeludo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 🚨 ¡IMPORTANTE! 🚨
    // Si usas el EMULADOR de Android Studio, tu PC es "10.0.2.2"
    // Si usas tu celular físico por USB/WiFi, pon la IP real de tu PC (ej. "192.168.1.50")
    private const val BASE_URL = "http://192.168.1.80:8000/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}