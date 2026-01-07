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

// 1. Agregamos los nuevos campos a la configuración
data class FiltrosInventario(
    var textoBusqueda: String = "",
    var especie: String = "Todos",   // Nuevo
    var categoria: String = "Todas", // Nuevo
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

            // 1. Texto (Buscador)
            val coincideTexto = nombre.contains(filtrosActuales.textoBusqueda.lowercase())

            // 2. Especie (Buscamos la palabra clave en el nombre por ahora)
            val coincideEspecie = if (filtrosActuales.especie == "Todos") true else {
                nombre.contains(filtrosActuales.especie.lowercase())
            }

            // 3. Categoría (Igual, buscamos palabra clave)
            // Nota: Esto mejorará cuando el backend mande 'categoria_id'
            val coincideCategoria = if (filtrosActuales.categoria == "Todas") true else {
                // Truco: Si filtramos "Farmacia", buscamos palabras relacionadas
                when (filtrosActuales.categoria) {
                    "Farmacia" -> nombre.contains("medic") || nombre.contains("vacuna") || nombre.contains("jarabe")
                    "Accesorio" -> nombre.contains("collar") || nombre.contains("correa") || nombre.contains("juguete")
                    "Alimento" -> !nombre.contains("collar") // Asumimos alimento si no es accesorio
                    else -> nombre.contains(filtrosActuales.categoria.lowercase())
                }
            }

            // 4. Estados
            val pasaBajoStock = if (filtrosActuales.soloBajoStock) prod.stock <= 5 else true
            val pasaGranel = if (filtrosActuales.soloGranel) prod.esGranel else true

            coincideTexto && coincideEspecie && coincideCategoria && pasaBajoStock && pasaGranel
        }

        // 5. Ordenamiento
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
            tvMarca.text = "ID: ${item.id}"
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            tvPrecio.text = format.format(item.precioBase)

            val unidad = if (item.esGranel) "kg" else "pzas"
            chipStock.text = "${item.stock} $unidad"

            if (item.stock <= 5.0) {
                chipStock.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#D32F2F"))
                chipStock.text = "¡Bajo! ${item.stock}"
            } else {
                chipStock.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#388E3C"))
            }
        }
    }
}