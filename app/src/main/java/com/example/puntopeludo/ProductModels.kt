package com.example.puntopeludo

import com.google.gson.annotations.SerializedName

// 1. CREAR (POST) - Agregamos stock_minimo y aseguramos SerializedName
data class CrearProductoRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("tipo_producto") val tipoProducto: String,
    @SerializedName("unidad_medida") val unidadMedida: String,
    @SerializedName("precio_base") val precioBase: Double,
    @SerializedName("precio_granel") val precioGranel: Double?,
    @SerializedName("contenido_neto") val contenidoNeto: Double,
    @SerializedName("se_vende_a_granel") val seVendeAGranel: Boolean,
    @SerializedName("marca_id") val marcaId: Int?,
    @SerializedName("categoria_id") val categoriaId: Int?,
    @SerializedName("especie_id") val especieId: Int?,
    @SerializedName("etapa_id") val etapaId: Int?,
    @SerializedName("stock_minimo") val stockMinimo: Double // Agregado: Es obligatorio en tu ProductoIn de Python
)

// 2. AUXILIARES
data class MarcaIn(val nombre: String)
data class CategoriaIn(val nombre: String)
data class EspecieIn(val nombre: String)
data class EtapaIn(val nombre: String)
data class LineaIn(val nombre: String)

// 3. LEER (GET) - Sin cambios, está correcto
data class ProductoResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("tipo_producto") val tipoProducto: String?,
    @SerializedName("unidad_medida") val unidadMedida: String?,
    @SerializedName("precio_base") val precioBase: Double,
    @SerializedName("precio_granel") val precioGranel: Double?,
    @SerializedName("stock_actual") val stock: Double,
    @SerializedName("se_vende_a_granel") val esGranel: Boolean,
    @SerializedName("stock_minimo") val stockMinimo: Double?,
    @SerializedName("contenido_neto") val contenidoNeto: Double?,
    @SerializedName("marca_id") val marcaId: Int?,
    @SerializedName("especie_id") val especieId: Int?,
    @SerializedName("marca_nombre") val marcaNombre: String?, // Agregado para filtros dinámicos
    @SerializedName("categoria_nombre") val categoriaNombre: String?,
    @SerializedName("especie_nombre") val especieNombre: String?,
    @SerializedName("categoria_id") val categoriaId: Int?,
    @SerializedName("etapa_id") val etapaId: Int?,
    @SerializedName("activo") val activo: Boolean,

    )

// 4. ACTUALIZAR STOCK (PATCH)
data class ActualizarStockRequest(
    @SerializedName("stock") val nuevoStock: Double
)

// 5. EDITAR DETALLES (PUT) - IMPORTANTE: Faltaban los SerializedName
data class EditarProductoRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("contenido_neto") val contenidoNeto: Double,
    @SerializedName("stock_minimo") val stockMinimo: Double,
    @SerializedName("precio_base") val precioBase: Double,
    @SerializedName("precio_granel") val precioGranel: Double? = null,
    @SerializedName("activo") val activo: Boolean = true,
    @SerializedName("es_granel") val esGranel: Boolean = false,

)

// Agrega esto en ProductoModels.kt
data class TipoProductoIn(
    @SerializedName("nombre") val nombre: String
)

data class TipoProducto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String
)

// En ProductoModels.kt
data class ClienteIn(
    val nombre: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val notas: String? = null
)

data class Cliente(
    val id: Int,
    val nombre: String,
    val telefono: String?,
    val direccion: String?,
    val notas: String?
)

// En ProductoModels.kt (o donde guardes tus modelos de Kotlin)

// === CONFIGURACIÓN DE DESCUENTOS ===

data class ReglaDescuentoIn(
    val descripcion: String,
    @SerializedName("descuento_porcentaje") val descuentoPorcentaje: Double,
    @SerializedName("cliente_id") val clienteId: Int? = null,
    @SerializedName("marca_id") val marcaId: Int? = null,
    @SerializedName("producto_id") val productoId: Int? = null,
    val activo: Boolean = true
)

data class ReglaDescuento(
    val id: Int,
    val descripcion: String,
    @SerializedName("descuento_porcentaje") val descuentoPorcentaje: Double,
    @SerializedName("cliente_id") val clienteId: Int?,
    @SerializedName("marca_id") val marcaId: Int?,
    @SerializedName("producto_id") val productoId: Int?,
    val activo: Boolean
)

data class IngresoInventarioIn(
    val producto_id: Int,
    val sucursal_id: Int,
    val cantidad: Double,
    val usuario_id: Int
)

data class IngresoInventario(
    val id: Int,
    val fecha_actualizacion: String
)

data class ProductoSurtido(
    val id: Int,
    val nombre: String,
    val cantidad: Double
)

// Modelos para el Corte de Caja
data class AperturaCajaReq(
    val sucursal_id: Int,
    val usuario_id: Int,
    val fondo_inicial: Double
)

data class CierreCajaReq(
    val corte_id: Int,
    val efectivo_real: Double,
    val monto_retirado: Double,
    val comentarios: String? = null
)

data class CorteResponse(
    val id: Int,
    val fecha_apertura: String,
    val fecha_cierre: String?,
    val fondo_inicial: Double,
    val ventas_totales: Double,
    val efectivo_esperado: Double,
    val efectivo_real: Double?,
    val diferencia: Double?,
    val fondo_siguiente: Double?,
    val estado: String // "ABIERTO" o "CERRADO"
)


// En ProductModels.kt
data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("usuario_id") val usuarioId: Int,   // Debe ser igual al de Python
    @SerializedName("sucursal_id") val sucursalId: Int, // Debe ser igual al de Python
    @SerializedName("nombre") val nombre: String,           // <--- AÑADIDO
    @SerializedName("sucursal_nombre") val sucursalNombre: String
)



data class VentaResponse(
    val id: Int,
    val fecha: String,
    val total: Double
)

data class HistorialCorteResponse(
    val id: Int,
    val fecha_apertura: String,
    val fecha_cierre: String?,
    val fondo_inicial: Double,
    val ventas_totales: Double,
    val efectivo_esperado: Double,
    val efectivo_real: Double?,
    val diferencia: Double?,
    val estado: String
)

// En ProductModels.kt - Estas son las únicas que deben existir
data class ProductoCarrito(
    @SerializedName("producto_id") val producto_id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("cantidad") var cantidad: Double,
    @SerializedName("precio_unitario") val precio_unitario: Double,
    @SerializedName("es_granel") val es_granel: Boolean
)

// Sustituye estas clases en ProductModels.kt
data class VentaIn(
    @SerializedName("sucursal_id") val sucursal_id: Int,
    @SerializedName("usuario_id") val usuario_id: Int,
    @SerializedName("cliente_id") val cliente_id: Int? = null,
    @SerializedName("corte_caja_id") val corte_caja_id: Int? = null,
    @SerializedName("total") val total: Double,
    @SerializedName("descuento_especial_monto") val descuento_especial_monto: Double = 0.0,
    @SerializedName("descuento_especial_motivo") val descuento_especial_motivo: String? = null
)

data class VentaDetalleIn(
    @SerializedName("venta_id") val venta_id: Int,
    @SerializedName("producto_id") val producto_id: Int,
    @SerializedName("cantidad") val cantidad: Double,
    @SerializedName("precio_unitario") val precio_unitario: Double
)

data class VentaCompletaIn(
    @SerializedName("venta") val venta: VentaIn,
    @SerializedName("detalles") val detalles: List<VentaDetalleIn>
)
// ProductModels.kt
data class VentaCreateReq(
    @SerializedName("sucursal_id") val sucursal_id: Int,
    @SerializedName("usuario_id") val usuario_id: Int,
    @SerializedName("cliente_id") val cliente_id: Int? = null,
    @SerializedName("detalles") val detalles: List<DetalleVentaReq>,
    @SerializedName("descuento_especial") val descuento_especial: Double = 0.0,
    @SerializedName("motivo_descuento") val motivo_descuento: String? = null
)

data class DetalleVentaReq(
    @SerializedName("producto_id") val producto_id: Int,
    @SerializedName("cantidad") val cantidad: Double
)
typealias Producto = ProductoResponse