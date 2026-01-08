package com.example.puntopeludo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClientesAdapter(
    private var clientes: MutableList<Cliente>,
    private val onOpcionesClick: (View, Cliente) -> Unit
) : RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder>() {

    // En ClientesAdapter.kt cambia el ViewHolder así:
    class ClienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCliente)
        val tvTelefono: TextView = view.findViewById(R.id.tvTelefonoCliente)
        // Cambia tvNota por el nombre que uses, asegurando que el ID sea tvRfcCliente
        val tvDireccion: TextView = view.findViewById(R.id.tvRfcCliente)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditarCliente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.tvNombre.text = cliente.nombre
        holder.tvTelefono.text = cliente.telefono ?: "Sin teléfono"
        holder.tvDireccion.text = cliente.direccion ?: "Sin dirección" //

        holder.btnEditar.setOnClickListener { view ->
            onOpcionesClick(view, cliente)
        }
    }

    override fun getItemCount() = clientes.size

    fun actualizarLista(nuevaLista: List<Cliente>) {
        clientes.clear()
        clientes.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}