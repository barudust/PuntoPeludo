package com.example.puntopeludo

import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.Response // <--- ESTA FALTABA
import retrofit2.http.DELETE // <--- ESTA FALTABA
import retrofit2.http.PATCH // <--- ESTA FALTABA
import retrofit2.http.Query


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

    @GET("categorias/")
    suspend fun getCategorias(): List<Categoria>

    @GET("marcas/")
    suspend fun getMarcas(): List<Marca>

    @GET("especies/")
    suspend fun getEspecies(): List<Especie>

    @GET("etapas/")
    suspend fun getEtapas(): List<Etapa>



    @GET("unidades-medida") suspend fun getUnidadesMedida(): List<String>

    @GET("productos/")
    suspend fun getProductos(): List<ProductoResponse>

    @POST("marcas/") // Agregada /
    suspend fun crearMarca(@Body marca: MarcaIn): Marca

    @POST("categorias/") // Agregada /
    suspend fun crearCategoria(@Body categoria: CategoriaIn): Categoria

    @POST("especies/") // Agregada /
    suspend fun crearEspecie(@Body especie: EspecieIn): Especie

    @POST("etapas/") // Agregada /
    suspend fun crearEtapa(@Body etapa: EtapaIn): Etapa

    // 1. ELIMINAR
    @DELETE("productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: Int): Response<Void>

    // 2. ACTUALIZAR STOCK (Usamos PATCH para modificar solo un campo)
    @PATCH("productos/{id}")
    suspend fun actualizarStock(
        @Path("id") id: Int,
        @Body datos: ActualizarStockRequest
    ): ProductoResponse

    // En ApiService.kt

    @GET("productos/") // Este dices que ya funciona así
    suspend fun obtenerProductos(
        @retrofit2.http.Query("mostrar_inactivos") mostrarInactivos: Boolean = false
    ): List<ProductoResponse>

    // En ApiService.kt
    @PUT("productos/{id}/") // Usamos la diagonal para evitar el redireccionamiento 307
    suspend fun editarProducto(
        @Path("id") id: Int,
        @Body request: EditarProductoRequest
    ): Response<ProductoResponse>

    @POST("tipos-producto/")
    suspend fun crearTipoProducto(@Body tipo: TipoProductoIn): TipoProducto
    // En ApiService.kt

    @DELETE("tipos-producto/{id}")
    suspend fun eliminarTipoProducto(@Path("id") id: Int): Response<Void>
    @DELETE("marcas/{id}")
    suspend fun eliminarMarca(@Path("id") id: Int): Response<Void>

    @DELETE("categorias/{id}")
    suspend fun eliminarCategoria(@Path("id") id: Int): Response<Void>

    @DELETE("especies/{id}")
    suspend fun eliminarEspecie(@Path("id") id: Int): Response<Void>

    @DELETE("etapas/{id}")
    suspend fun eliminarEtapa(@Path("id") id: Int): Response<Void>
    // Busca esta línea y cámbiala:
    @GET("tipos-producto/")
    suspend fun getTiposProducto(): List<TipoProducto> // Antes decía List<String>

    // En ApiService.kt
    @GET("clientes/")
    suspend fun getClientes(): List<Cliente>

    @POST("clientes/")
    suspend fun crearCliente(@Body cliente: ClienteIn): Cliente

    @DELETE("clientes/{id}")
    suspend fun eliminarCliente(@Path("id") id: Int): Response<Void>


    @GET("descuentos/")
    suspend fun getDescuentos(): List<ReglaDescuento> // Cambiado de Descuento

    @POST("descuentos/")
    suspend fun crearDescuento(@Body regla: ReglaDescuentoIn): ReglaDescuento
    @DELETE("descuentos/{id}")
    suspend fun eliminarRegla(@Path("id") id: Int): retrofit2.Response<Void>

    // Asegúrate de que este método exista para VentaActivity


    @PUT("clientes/{id}")
    suspend fun actualizarCliente(@Path("id") id: Int, @Body cliente: ClienteIn): Cliente
    @POST("descuentos/")
    suspend fun crearRegla(@Body regla: ReglaDescuentoIn): ReglaDescuento
    // En ApiService.kt
    @POST("ingreso-inventario/")
    suspend fun surtirProducto(@Body ingreso: IngresoInventarioIn): IngresoInventario
    // === ENDPOINTS DE CORTE DE CAJA ===
    @POST("corte/abrir")
    suspend fun abrirCaja(@Body req: AperturaCajaReq): CorteResponse

    @GET("corte/actual/{usuario_id}")
    suspend fun getCorteActual(@Path("usuario_id") usuarioId: Int): CorteResponse

    @POST("corte/cerrar")
    suspend fun cerrarCaja(@Body req: CierreCajaReq): CorteResponse
    // Endpoints de Ventas
    @POST("ventas/") // Ajusta la ruta según tus routers de FastAPI
    suspend fun crearVenta(@Body venta: VentaIn): VentaResponse

    @POST("ventas/detalle") // Ajusta la ruta según tus routers de FastAPI
    suspend fun registrarDetalleVenta(@Body detalle: VentaDetalleIn): Any
    // CORRECTO PARA CAJA

    @GET("corte/historial/{usuario_id}")
    suspend fun getHistorialCortes(@Path("usuario_id") usuarioId: Int): List<HistorialCorteResponse>

    // Búsqueda de catálogos


    @POST("ventas/detalle")
    suspend fun registrarDetalle(@Body detalle: VentaDetalleIn): Any
    @POST("ventas/")
    suspend fun crearVenta(@Body ventaCompleta: VentaCompletaIn): VentaResponse
    @POST("ventas/")
    suspend fun registrarVenta(@Body data: VentaCreateReq): Map<String, Any>

    // ... dentro de interface ApiService ...

    // REPORTES
    @GET("informes/reporte-ventas")
    suspend fun getReporteVentas(
        @Query("sucursal_id") sucursalId: Int,
        @Query("inicio") inicio: String, // Formato YYYY-MM-DD
        @Query("fin") fin: String
    ): List<ReporteVentaItem>

    @GET("informes/reporte-surtidos")
    suspend fun getReporteSurtidos(
        @Query("sucursal_id") sucursalId: Int,
        @Query("inicio") inicio: String,
        @Query("fin") fin: String
    ): List<ReporteSurtidoBloque>

    @GET("informes/reporte-cortes")
    suspend fun getReporteCortes(
        @Query("sucursal_id") sucursalId: Int,
        @Query("inicio") inicio: String,
        @Query("fin") fin: String
    ): List<ReporteCorteItem>

    // ... dentro de interface ApiService ...

    // SUCURSALES (Para el registro)
    @GET("sucursales/")
    suspend fun getSucursales(): List<Sucursal>

    // USUARIOS (Para registrarse)
    @POST("usuarios/")
    suspend fun registrarUsuario(@Body usuario: UsuarioRegistroIn): UsuarioResponse

}