package com.example.puntopeludo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductoAdapter(private var lista: List<InventarioItem>) :
    RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.textViewNombreProducto)
        val tvCantidad: TextView = view.findViewById(R.id.textViewStockBultos)
        val tvPrecio: TextView = view.findViewById(R.id.textViewStockKilos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        // --- A. Nombre ---
        holder.tvNombre.text = item.nombre

        // --- B. Lógica de Conversión (Bultos vs Unidades Reales) ---
        var cantidadVisual = item.cantidad
        val unidadVisual = item.unidadMedida

        // Si es Bulto/Saco, la "cantidadVisual" son los paquetes, no los kilos totales
        if ((item.unidadMedida == "Bulto" || item.unidadMedida == "Saco") && item.contenidoNeto > 0) {
            cantidadVisual = item.cantidad / item.contenidoNeto
        }

        // --- C. Stock (Cantidad Grande) ---
        // Lista de cosas que se cuentan por pieza entera (sin decimales)
        val esPieza = item.unidadMedida == "Pieza" || item.unidadMedida == "Bote" ||
                item.unidadMedida == "Collar" || item.unidadMedida == "Lata"

        val cantidadTexto = if (esPieza) {
            cantidadVisual.toInt().toString() // "20"
        } else {
            String.format("%.1f", cantidadVisual) // "20.5"
        }

        holder.tvCantidad.text = "Stock: $cantidadTexto $unidadVisual"

        // --- D. Color de Alerta ---
        if (cantidadVisual < 3.0) {
            holder.tvCantidad.setTextColor(Color.RED)
        } else {
            holder.tvCantidad.setTextColor(Color.BLACK)
        }

        // --- E. Subtítulo (Precio y Detalle Físico) ---
        if (esPieza) {
            // Si es pieza, solo precio
            holder.tvPrecio.text = "Precio: $${item.precio}"
        } else {
            // AQUÍ ESTABA EL ERROR. Nueva lógica "inteligente":
            val u = item.unidadMedida.uppercase().trim()

            val sufijoReal = when {
                // 1. Si suena a líquido -> L
                u.contains("LITR") || u == "L" || u == "ML" || u == "LT" -> "L"

                // 2. Si suena a peso/granel -> kg
                // Nota: Incluimos BULTO y SACO aquí para que muestren el peso total en kg
                u.contains("KILO") || u == "KG" || u == "G" || u == "BULTO" || u == "SACO" || u == "GRANEL" -> "kg"

                // 3. CASO DE SEGURIDAD (Si no es ni agua ni peso)
                // Antes aquí poníamos "kg" y por eso fallaba.
                // Ahora ponemos la unidad real. Ej: Si la unidad es "Garrafa", dirá "Garrafa".
                else -> item.unidadMedida
            }

            holder.tvPrecio.text = "($${item.precio}) - Total Físico: ${item.cantidad} $sufijoReal"
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<InventarioItem>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}