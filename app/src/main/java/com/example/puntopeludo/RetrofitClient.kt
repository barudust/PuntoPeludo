package com.example.puntopeludo

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    // Configuramos un GSON inteligente
    private val gson = GsonBuilder()
        // 1. Esto envía cliente_id: null en lugar de borrar la línea
        .serializeNulls()
        // 2. Esto asegura que usuarioId en Kotlin se convierta en usuario_id en JSON automáticamente
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Usamos nuestra configuración personalizada de GSON
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}