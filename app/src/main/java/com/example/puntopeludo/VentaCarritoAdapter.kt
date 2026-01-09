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
        // NOTA: Debes cambiar tvCantidad a EditText en tu XML item_carrito_venta.xml
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
        holder.nombre.text = item.nombre
        holder.cantidad.setText(item.cantidad.toString())

        val totalItem = item.cantidad * item.precio_unitario
        holder.subtotal.text = String.format(Locale.US, "$%.2f", totalItem)

        // Lógica de Granel: Permite decimales si el producto lo requiere
        if (item.es_granel) {
            holder.cantidad.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        } else {
            holder.cantidad.inputType = InputType.TYPE_CLASS_NUMBER
        }

        // Actualizar cantidad al escribir manualmente
        holder.cantidad.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val nuevaCant = holder.cantidad.text.toString().toDoubleOrNull() ?: 1.0
                item.cantidad = nuevaCant
                notifyItemChanged(position)
                onTotalChanged()
            }
        }

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
                items.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, items.size)
            }
            onTotalChanged()
        }
    }

    override fun getItemCount() = items.size
    fun obtenerLista() = items

    fun agregarProducto(p: ProductoCarrito) {
        val existente = items.find { it.producto_id == p.producto_id }
        if (existente != null) {
            existente.cantidad += 1.0
        } else {
            items.add(p)
        }
        notifyDataSetChanged() // <--- CRÍTICO PARA QUE SE VEA EN PANTALLA
        onTotalChanged()
    }
}