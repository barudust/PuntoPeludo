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
    @SerializedName("etapa_id") val etapaId: Int?
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
    @SerializedName("precio_base") val precioBase: Double
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

typealias Producto = ProductoResponse