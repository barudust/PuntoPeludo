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
    var mostrarInactivos: Boolean = false, // <--- NUEVO FILTRO
    var orden: TipoOrden = TipoOrden.DEFECTO
)

enum class TipoOrden { DEFECTO, PRECIO_ASC, PRECIO_DESC }

class InventarioAdapter(
    private val onProductoClick: (ProductoResponse) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.ProductoViewHolder>() {

    private var listaCompleta = listOf<ProductoResponse>()
    private var listaMostrar = listOf<ProductoResponse>()
    private var filtrosActuales = FiltrosInventario()

    // MAPAS PARA TRADUCIR IDs a NOMBRES
    private var mapaMarcas = mapOf<Int, String>()
    private var mapaCategorias = mapOf<Int, String>() // Usaremos IDs reales de categoría
    private var mapaEspecies = mapOf<Int, String>()

    fun setDatos(
        productos: List<ProductoResponse>,
        marcas: Map<Int, String>,
        categorias: Map<Int, String>,
        especies: Map<Int, String>
    ) {
        this.listaCompleta = productos
        this.mapaMarcas = marcas
        this.mapaCategorias = categorias
        this.mapaEspecies = especies
        aplicarLogicaFiltros()
    }

    fun actualizarFiltros(nuevosFiltros: FiltrosInventario) {
        this.filtrosActuales = nuevosFiltros
        aplicarLogicaFiltros()
    }

    private fun aplicarLogicaFiltros() {
        var resultado = listaCompleta.filter { prod ->
            // 0. Filtro de Activo/Inactivo (Papelera)
            val estadoCoincide = if (filtrosActuales.mostrarInactivos) !prod.activo else prod.activo

            if (!estadoCoincide) return@filter false

            val nombre = prod.nombre.lowercase()

            // 1. Texto
            val coincideTexto = nombre.contains(filtrosActuales.textoBusqueda.lowercase())

            // 2. Especie (Usando el Mapa)
            val nombreEspecie = mapaEspecies[prod.especieId] ?: "Sin Especie"
            val coincideEspecie = if (filtrosActuales.especie == "Todos") true else nombreEspecie == filtrosActuales.especie

            // 3. Categoría (Usando el Mapa o Tipo como fallback)
            val nombreCategoria = mapaCategorias[prod.categoriaId] ?: (prod.tipoProducto ?: "Otros")
            val coincideCategoria = if (filtrosActuales.categoria == "Todas") true else nombreCategoria.equals(filtrosActuales.categoria, ignoreCase = true)

            // 4. Marca (Usando el Mapa)
            val nombreMarca = mapaMarcas[prod.marcaId] ?: "Sin Marca"
            val coincideMarca = if (filtrosActuales.marca == "Todas") true else nombreMarca == filtrosActuales.marca

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
        // Pasamos los mapas al ViewHolder para pintar bonito
        holder.bind(item, mapaMarcas, mapaCategorias)
        holder.itemView.setOnClickListener { onProductoClick(item) }
    }

    override fun getItemCount(): Int = listaMostrar.size

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreProd)
        private val tvMarca = itemView.findViewById<TextView>(R.id.tvMarcaProd)
        private val tvPrecio = itemView.findViewById<TextView>(R.id.tvPrecioProd)
        private val chipStock = itemView.findViewById<Chip>(R.id.chipStock)
        private val tvCategoria = itemView.findViewById<TextView>(R.id.tvCategoriaProd)

        fun bind(item: ProductoResponse, marcas: Map<Int, String>, categorias: Map<Int, String>) {
            tvNombre.text = item.nombre

            // TRADUCCIÓN DE ID A NOMBRE VISUAL
            val nombreMarca = marcas[item.marcaId] ?: "Sin Marca"
            tvMarca.text = "Marca: $nombreMarca"

            val nombreCat = categorias[item.categoriaId] ?: (item.tipoProducto ?: "General")
            tvCategoria.text = "Cat: $nombreCat"

            val format = NumberFormat.getCurrencyInstance(Locale.US)
            tvPrecio.text = format.format(item.precioBase)

            // Si está eliminado, lo mostramos visualmente diferente
            if (!item.activo) {
                tvNombre.setTextColor(Color.GRAY)
                tvNombre.text = "🚫 ${item.nombre} (Eliminado)"
                chipStock.visibility = View.GONE
            } else {
                tvNombre.setTextColor(Color.parseColor("#0D47A1")) // Azul default
                chipStock.visibility = View.VISIBLE
            }

            // Lógica de Stock (Solo si activo)
            if (item.activo) {
                val rawUnidad = item.unidadMedida ?: ""
                val u = rawUnidad.uppercase().trim()
                val contenido = item.contenidoNeto ?: 1.0
                val textoStock: String
                val cantidadParaAlerta: Double

                if ((u.contains("BULTO") || u.contains("SACO") || u.contains("CAJA")) && contenido > 0) {
                    val cantidadPaquetes = item.stock / contenido
                    val paquetesTxt = String.format("%.1f", cantidadPaquetes)
                    val sufijoBase = if (u.contains("CAJA")) "pzas" else "kg"
                    textoStock = "${item.stock.toInt()} $sufijoBase ($paquetesTxt $rawUnidad)"
                    cantidadParaAlerta = cantidadPaquetes
                } else {
                    textoStock = "${item.stock} $rawUnidad"
                    cantidadParaAlerta = item.stock
                }

                chipStock.text = textoStock
                val minimo = item.stockMinimo ?: 5.0

                if (cantidadParaAlerta <= minimo) {
                    chipStock.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#D32F2F"))
                    chipStock.setTextColor(Color.WHITE)
                } else {
                    chipStock.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#388E3C"))
                    chipStock.setTextColor(Color.WHITE)
                }
            }
        }
    }
}