package com.example.puntopeludo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductoAdapter(private var lista: List<InventarioItem>) :
    RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    // 1. El "Molde" del renglón (Busca los IDs de los textos)
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.textViewNombreProducto)
        val tvCantidad: TextView = view.findViewById(R.id.textViewStockBultos)
        val tvPrecio: TextView = view.findViewById(R.id.textViewStockKilos)
    }

    // 2. Crea renglones vacíos cuando se necesitan
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        // A. Nombre
        holder.tvNombre.text = item.nombre

        // B. Lógica de Conversión (Bultos vs Unidades Reales)
        var cantidadVisual = item.cantidad
        var unidadVisual = item.unidadMedida

        // Si es Bulto/Saco, convertimos Kilos -> Paquetes
        if ((item.unidadMedida == "Bulto" || item.unidadMedida == "Saco") && item.contenidoNeto > 0) {
            cantidadVisual = item.cantidad / item.contenidoNeto
        }

        // C. Formato de Texto (Quitar decimales si es Pieza)
        val cantidadTexto = if (item.unidadMedida == "Pieza" || item.unidadMedida == "Bote" || item.unidadMedida == "Collar") {
            // Si es pieza, lo convertimos a entero (Ej: "20" en vez de "20.0")
            cantidadVisual.toInt().toString()
        } else {
            // Si es peso o volumen, dejamos 1 decimal (Ej: "3.5")
            String.format("%.1f", cantidadVisual)
        }

        holder.tvCantidad.text = "Stock: $cantidadTexto $unidadVisual"

        // D. Alerta Visual (Rojo si queda poco)
        if (cantidadVisual < 3.0) {
            holder.tvCantidad.setTextColor(Color.RED)
        } else {
            holder.tvCantidad.setTextColor(Color.BLACK)
        }

        // E. Subtítulo Inteligente (Precio y Detalle)
        if (item.unidadMedida == "Pieza" || item.unidadMedida == "Bote") {
            // Si es pieza, solo mostramos el precio
            holder.tvPrecio.text = "Precio: $${item.precio}"
        } else {
            // Si es granel/bulto, mostramos el precio y el total real en kilos
            // Esto ayuda a saber que "3.5 Bultos" son "140 kg" reales
            holder.tvPrecio.text = "($${item.precio}) - Total Físico: ${item.cantidad} kg"
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<InventarioItem>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}