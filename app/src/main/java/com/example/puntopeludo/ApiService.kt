package com.example.puntopeludo

import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    // Login
    @FormUrlEncoded
    @POST("token")
    suspend fun login(@Field("username") u: String, @Field("password") p: String): LoginResponse

    // Inventario
    @GET("inventario/reporte-sucursal/{id}")
    suspend fun obtenerInventarioSucursal(@Path("id") id: Int): List<InventarioItem>

    // Productos
    @POST("productos/")
    suspend fun crearProducto(@Body p: CrearProductoRequest): Producto

    // Catálogos
    @GET("marcas") suspend fun getMarcas(): List<Marca>
    @GET("categorias") suspend fun getCategorias(): List<Categoria>
    @GET("especies") suspend fun getEspecies(): List<Especie>
    @GET("etapas") suspend fun getEtapas(): List<Etapa>

    @GET("tipos-producto") suspend fun getTiposProducto(): List<String>
    @GET("unidades-medida") suspend fun getUnidadesMedida(): List<String>


    @POST("marcas")
    suspend fun crearMarca(@Body marca: MarcaIn): Marca

    @POST("categorias")
    suspend fun crearCategoria(@Body categoria: CategoriaIn): Categoria

    @POST("especies")
    suspend fun crearEspecie(@Body especie: EspecieIn): Especie

    @POST("etapas")
    suspend fun crearEtapa(@Body etapa: EtapaIn): Etapa


}