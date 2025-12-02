package com.example.puntopeludo

import com.google.gson.annotations.SerializedName

data class Producto(
    val id: Int,
    val nombre: String,
    @SerializedName("tipo_producto") val tipoProducto: String,
    @SerializedName("precio_base") val precioBase: Double,
    @SerializedName("precio_granel") val precioGranel: Double?,
    @SerializedName("contenido_neto") val contenidoNeto: Double,
    @SerializedName("unidad_medida") val unidadMedida: String,
    @SerializedName("se_vende_a_granel") val seVendeAGranel: Boolean,
    val activo: Boolean
)