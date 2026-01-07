package com.example.puntopeludo

import com.google.gson.annotations.SerializedName

// Modelo para CREAR un producto (Lo que enviamos al POST)
data class CrearProductoRequest(
    val nombre: String,

    @SerializedName("tipo_producto") // <-- Esto es para el JSON (Python)
    val tipoProducto: String,        // <-- Esto es para Kotlin

    @SerializedName("unidad_medida")
    val unidadMedida: String,

    @SerializedName("precio_base")
    val precioBase: Double,

    @SerializedName("precio_granel")
    val precioGranel: Double?,

    @SerializedName("contenido_neto")
    val contenidoNeto: Double,

    @SerializedName("se_vende_a_granel")
    val seVendeAGranel: Boolean,

    @SerializedName("marca_id")
    val marcaId: Int?,

    @SerializedName("categoria_id")
    val categoriaId: Int?,

    @SerializedName("especie_id")
    val especieId: Int?,

    @SerializedName("etapa_id")
    val etapaId: Int?
)
data class MarcaIn(val nombre: String)
data class CategoriaIn(val nombre: String)
data class EspecieIn(val nombre: String)
data class EtapaIn(val nombre: String)
data class LineaIn(val nombre: String)

// Este es el modelo para LEER productos (lo que recibes del servidor)
data class ProductoResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("precio_base") val precioBase: Double,
    @SerializedName("precio_granel") val precioGranel: Double?,
    @SerializedName("stock_actual") val stock: Double, // Ojo: el backend debe mandar esto
    @SerializedName("marca_id") val marcaId: Int?,
    @SerializedName("se_vende_a_granel") val esGranel: Boolean
    // Puedes agregar más campos si el backend los manda
)

// Modelo para enviar solo la actualización de stock
data class ActualizarStockRequest(
    @SerializedName("stock_actual") val nuevoStock: Double
)