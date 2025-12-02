package com.example.puntopeludo

import com.google.gson.annotations.SerializedName

// Modelo para CREAR un producto (Lo que enviamos al POST)
data class CrearProductoRequest(
    val nombre: String,
    @SerializedName("tipo_producto") val tipoProducto: String,
    @SerializedName("unidad_medida") val unidadMedida: String,
    @SerializedName("precio_base") val precioBase: Double,
    @SerializedName("precio_granel") val precioGranel: Double?,
    @SerializedName("contenido_neto") val contenidoNeto: Double,
    @SerializedName("se_vende_a_granel") val seVendeAGranel: Boolean,

    // IDs opcionales (¡Ahora sí incluimos todos!)
    @SerializedName("marca_id") val marcaId: Int? = null,
    @SerializedName("categoria_id") val categoriaId: Int? = null,
    @SerializedName("especie_id") val especieId: Int? = null, // <--- Faltaba este
    @SerializedName("etapa_id") val etapaId: Int? = null,       // <--- Faltaba este





)
data class MarcaIn(val nombre: String)
data class CategoriaIn(val nombre: String)
data class EspecieIn(val nombre: String)
data class EtapaIn(val nombre: String)
data class LineaIn(val nombre: String)