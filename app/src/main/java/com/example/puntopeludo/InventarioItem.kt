package com.example.puntopeludo
import com.google.gson.annotations.SerializedName

data class InventarioItem(
    val id: Int,
    val nombre: String,
    @SerializedName("unidad_medida") val unidadMedida: String,
    @SerializedName("precio_base") val precio: Double,
    @SerializedName("cantidad_actual") val cantidad: Double,
    @SerializedName("contenido_neto") val contenidoNeto: Double
)