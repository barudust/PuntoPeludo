package com.example.puntopeludo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

// Modelo simple para el carrito (Producto + Cantidad que lleva)
data class ProductoCarrito(
    val idProducto: Int,
    val nombre: String,
    val precioUnitario: Double,
    var cantidad: Double,
    val esGranel: Boolean
) {
    fun calcularSubtotal(): Double = precioUnitario * cantidad
}

class VentaAdapter(
    private val onEliminarClick: (ProductoCarrito) -> Unit
) : RecyclerView.Adapter<VentaAdapter.VentaViewHolder>() {

    private val listaProductos = ArrayList<ProductoCarrito>()

    // Función para agregar productos desde la Activity
    fun agregarProducto(producto: ProductoCarrito) {
        // Verificar si ya existe para solo sumar cantidad
        val existente = listaProductos.find { it.idProducto == producto.idProducto }
        if (existente != null) {
            existente.cantidad += producto.cantidad
        } else {
            listaProductos.add(producto)
        }
        notifyDataSetChanged()
    }

    // Función para obtener la lista final al cobrar
    fun obtenerLista(): List<ProductoCarrito> = listaProductos

    fun limpiarCarrito() {
        listaProductos.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_venta_producto, parent, false)
        return VentaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        val item = listaProductos[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = listaProductos.size

    inner class VentaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreProducto)
        private val tvDetalle = itemView.findViewById<TextView>(R.id.tvDetallePrecio)
        private val tvSubtotal = itemView.findViewById<TextView>(R.id.tvSubtotal)
        private val btnEliminar = itemView.findViewById<ImageButton>(R.id.btnEliminar)

        fun bind(item: ProductoCarrito) {
            tvNombre.text = item.nombre

            // Formato de moneda
            val format = NumberFormat.getCurrencyInstance(Locale.US)

            // Texto tipo: "$50.00 x 3.5 kg"
            val unidad = if (item.esGranel) "kg" else "pza"
            tvDetalle.text = "${format.format(item.precioUnitario)} x ${item.cantidad} $unidad"

            tvSubtotal.text = format.format(item.calcularSubtotal())

            btnEliminar.setOnClickListener {
                listaProductos.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                onEliminarClick(item) // Avisar a la Activity para recalcular total
            }
        }
    }
}