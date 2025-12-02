package com.example.puntopeludo

import com.google.gson.annotations.SerializedName

// Lo que enviamos al servidor (Usuario y Contraseña)
// Nota: FastAPI espera 'username' y 'password' por defecto en OAuth2
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

// Lo que el servidor nos responde (El Token)
data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)