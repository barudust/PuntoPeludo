package com.example.puntopeludo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SurtirAdapter(
    private val lista: MutableList<ProductoSurtido>,
    private val onEliminar: (Int) -> Unit,
    private val onEditar: (Int, ProductoSurtido) -> Unit
) : RecyclerView.Adapter<SurtirAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvNombreItem)
        val cantidad: TextView = view.findViewById(R.id.tvCantidadItem)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_surtido, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.nombre.text = item.nombre
        holder.cantidad.text = "Cantidad: ${item.cantidad}"

        holder.btnEliminar.setOnClickListener { onEliminar(holder.adapterPosition) }
        holder.itemView.setOnClickListener { onEditar(holder.adapterPosition, item) }
    }

    override fun getItemCount() = lista.size
}