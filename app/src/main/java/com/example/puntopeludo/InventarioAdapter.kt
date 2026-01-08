package com.example.puntopeludo

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.Locale

data class FiltrosInventario(
    var textoBusqueda: String = "",
    var especie: String = "Todos",
    var categoria: String = "Todas",
    var marca: String = "Todas",
    var soloBajoStock: Boolean = false,
    var soloGranel: Boolean = false,
    var orden: TipoOrden = TipoOrden.DEFECTO
)

enum class TipoOrden { DEFECTO, PRECIO_ASC, PRECIO_DESC }

class InventarioAdapter(
    private val onProductoClick: (ProductoResponse) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.ProductoViewHolder>() {

    private var listaCompleta = listOf<ProductoResponse>()
    private var listaMostrar = listOf<ProductoResponse>()
    private var filtrosActuales = FiltrosInventario()

    fun setProductos(nuevosProductos: List<ProductoResponse>) {
        this.listaCompleta = nuevosProductos
        aplicarLogicaFiltros()
    }

    fun actualizarFiltros(nuevosFiltros: FiltrosInventario) {
        this.filtrosActuales = nuevosFiltros
        aplicarLogicaFiltros()
    }

    private fun aplicarLogicaFiltros() {
        var resultado = listaCompleta.filter { prod ->
            val nombre = prod.nombre.lowercase()

            // 1. Texto
            val coincideTexto = nombre.contains(filtrosActuales.textoBusqueda.lowercase())

            // 2. Especie
            val prodEspecie = if (prod.especieId != null) "ID: ${prod.especieId}" else "Sin Especie"
            val coincideEspecie = if (filtrosActuales.especie == "Todos") true else prodEspecie == filtrosActuales.especie

            // 3. Categoría (Usando Tipo)
            val prodCategoria = prod.tipoProducto ?: "Otros"
            val coincideCategoria = if (filtrosActuales.categoria == "Todas") true else prodCategoria.equals(filtrosActuales.categoria, ignoreCase = true)

            // 4. Marca
            val prodMarca = if (prod.marcaId != null) "ID: ${prod.marcaId}" else "Sin Marca"
            val coincideMarca = if (filtrosActuales.marca == "Todas") true else prodMarca == filtrosActuales.marca

            // Alertas
            val contenido = prod.contenidoNeto ?: 1.0
            val u = (prod.unidadMedida ?: "").uppercase()
            val esPaquete = u.contains("BULTO") || u.contains("SACO") || u.contains("CAJA")
            val cantidadReal = if (esPaquete && contenido > 0) prod.stock / contenido else prod.stock
            val minimo = prod.stockMinimo ?: 5.0
            val esBajo = cantidadReal <= minimo

            val pasaBajoStock = if (filtrosActuales.soloBajoStock) esBajo else true
            val pasaGranel = if (filtrosActuales.soloGranel) prod.esGranel else true

            coincideTexto && coincideEspecie && coincideCategoria && coincideMarca && pasaBajoStock && pasaGranel
        }

        resultado = when (filtrosActuales.orden) {
            TipoOrden.PRECIO_ASC -> resultado.sortedBy { it.precioBase }
            TipoOrden.PRECIO_DESC -> resultado.sortedByDescending { it.precioBase }
            else -> resultado
        }

        listaMostrar = resultado
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_inventario, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val item = listaMostrar[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onProductoClick(item) }
    }

    override fun getItemCount(): Int = listaMostrar.size

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreProd)
        private val tvMarca = itemView.findViewById<TextView>(R.id.tvMarcaProd)
        private val tvPrecio = itemView.findViewById<TextView>(R.id.tvPrecioProd)
        private val chipStock = itemView.findViewById<Chip>(R.id.chipStock)

        fun bind(item: ProductoResponse) {
            tvNombre.text = item.nombre
            val marcaTexto = if (item.marcaId != null) "Marca ID: ${item.marcaId}" else "Sin Marca"
            tvMarca.text = marcaTexto

            val format = NumberFormat.getCurrencyInstance(Locale.US)
            tvPrecio.text = format.format(item.precioBase)

            // Lógica Visual
            val rawUnidad = item.unidadMedida ?: ""
            val u = rawUnidad.uppercase().trim()
            val contenido = item.contenidoNeto ?: 1.0
            val textoStock: String
            val cantidadParaAlerta: Double

            if ((u.contains("BULTO") || u.contains("SACO") || u.contains("CAJA")) && contenido > 0) {
                val cantidadPaquetes = item.stock / contenido
                val paquetesTxt = String.format("%.1f", cantidadPaquetes)
                val sufijoBase = if (u.contains("CAJA")) "pzas" else "kg"
                val stockVisual = if (u.contains("CAJA")) item.stock.toInt().toString() else item.stock.toString()
                textoStock = "$stockVisual $sufijoBase ($paquetesTxt $rawUnidad)"
                cantidadParaAlerta = cantidadPaquetes
            } else if (u.contains("LITR") || u == "L" || u == "LT" || u == "ML") {
                textoStock = "${item.stock} L"
                cantidadParaAlerta = item.stock
            } else if (u.contains("KILO") || u == "KG" || u == "G") {
                textoStock = "${item.stock} kg"
                cantidadParaAlerta = item.stock
            } else {
                textoStock = "${item.stock.toInt()} pzas"
                cantidadParaAlerta = item.stock
            }

            chipStock.text = textoStock
            val minimo = item.stockMinimo ?: 5.0

            if (cantidadParaAlerta <= minimo) {
                chipStock.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#D32F2F"))
                chipStock.text = "¡Bajo! $textoStock"
                chipStock.setTextColor(Color.WHITE)
            } else {
                chipStock.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#388E3C"))
                chipStock.setTextColor(Color.WHITE)
            }
        }
    }
}