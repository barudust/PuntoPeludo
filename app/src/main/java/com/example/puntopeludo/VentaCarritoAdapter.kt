package com.example.puntopeludo

import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class VentaCarritoAdapter(
    private val items: MutableList<ProductoCarrito>,
    private val onTotalChanged: () -> Unit
) : RecyclerView.Adapter<VentaCarritoAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val nombre: TextView = v.findViewById(R.id.tvProductoNombre)
        val cantidad: EditText = v.findViewById(R.id.tvCantidad)
        val subtotal: TextView = v.findViewById(R.id.tvSubtotal)
        val btnMas: ImageButton = v.findViewById(R.id.btnMas)
        val btnMenos: ImageButton = v.findViewById(R.id.btnMenos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_carrito_venta, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Mostrar si es Granel o Paquete
        val tipo = if (item.es_granel) " (Granel/Kg)" else " (Paquete)"
        holder.nombre.text = item.nombre + tipo

        // Evitamos loop infinito de listeners al reciclar
        holder.cantidad.setOnFocusChangeListener(null)

        // Formato de cantidad (quita decimales .0 si es entero)
        if (item.cantidad % 1.0 == 0.0) {
            holder.cantidad.setText(item.cantidad.toInt().toString())
        } else {
            holder.cantidad.setText(item.cantidad.toString())
        }

        val totalItem = item.cantidad * item.precio_unitario
        holder.subtotal.text = String.format(Locale.US, "$%.2f", totalItem)

        // Configurar teclado
        if (item.es_granel) {
            holder.cantidad.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        } else {
            holder.cantidad.inputType = InputType.TYPE_CLASS_NUMBER
        }

        // Listeners Botones
        holder.btnMas.setOnClickListener {
            item.cantidad += 1.0
            notifyItemChanged(position)
            onTotalChanged()
        }

        holder.btnMenos.setOnClickListener {
            if (item.cantidad > 1.0) {
                item.cantidad -= 1.0
                notifyItemChanged(position)
            } else {
                // Si es 1 o menos (ej. 0.5kg), lo borramos al presionar menos
                items.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, items.size)
            }
            onTotalChanged()
        }

        // Listener Texto Manual
        holder.cantidad.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val texto = holder.cantidad.text.toString()
                val nuevaCant = texto.toDoubleOrNull() ?: 0.0
                if (nuevaCant > 0) {
                    item.cantidad = nuevaCant
                    onTotalChanged()
                    // No llamamos notifyItemChanged aquí para no perder el foco o cerrar teclado bruscamente
                    val nuevoTotal = item.cantidad * item.precio_unitario
                    holder.subtotal.text = String.format(Locale.US, "$%.2f", nuevoTotal)
                }
            }
        }
    }

    override fun getItemCount() = items.size
    fun obtenerLista() = items

    fun agregarProducto(p: ProductoCarrito) {
        // SOLUCIÓN AL DOBLE AGREGADO:
        // Aquí NO verificamos nada, confiamos ciegamente en la Activity.
        // Y solo hacemos UN add.
        items.add(p)
        notifyItemInserted(items.size - 1)
        onTotalChanged()
    }
}