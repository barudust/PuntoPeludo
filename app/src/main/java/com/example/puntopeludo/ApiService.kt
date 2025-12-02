package com.example.puntopeludo

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


// ¡OJO! NO importes java.util.List aquí.

interface ApiService {

    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("username") usuario: String,
        @Field("password") contrasena: String
    ): LoginResponse

    @GET("productos")
    suspend fun obtenerProductos(): List<Producto>

    @GET("inventario/reporte-sucursal/{sucursal_id}")
    suspend fun obtenerInventarioSucursal(
        @Path("sucursal_id") sucursalId: Int
    ): List<InventarioItem>
}