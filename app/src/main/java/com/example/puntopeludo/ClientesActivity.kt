package com.example.puntopeludo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ClientesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClientesAdapter

    // Listas en memoria
    private var listaClientes = mutableListOf<Cliente>()
    private var listaReglas = mutableListOf<ReglaDescuento>() // <--- AQUÍ GUARDAMOS LOS DESCUENTOS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes)

        // 1. Configurar el listado
        recyclerView = findViewById(R.id.recyclerViewClientes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ClientesAdapter(listaClientes) { view, cliente ->
            mostrarOpcionesCliente(view, cliente)
        }
        recyclerView.adapter = adapter

        // 2. Botones
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.fabAgregarCliente).setOnClickListener { mostrarDialogoNuevoCliente() }

        cargarDatos()
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            try {
                // CORRECCIÓN: Cargamos Clientes Y Descuentos al mismo tiempo
                val clientes = RetrofitClient.instance.getClientes()
                val reglas = RetrofitClient.instance.getDescuentos()

                listaClientes.clear()
                listaClientes.addAll(clientes)

                listaReglas.clear()
                listaReglas.addAll(reglas)

                adapter.actualizarLista(clientes)
            } catch (e: Exception) {
                Log.e("CLIENTES_ERROR", "Error: ${e.message}")
                Toast.makeText(this@ClientesActivity, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoNuevoCliente(clienteAEditar: Cliente? = null) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(if (clienteAEditar == null) "Nuevo Cliente" else "Editar Cliente")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 10)
        }

        // --- CAMPOS ---
        val inputNombre = EditText(this).apply {
            hint = "Nombre"
            setText(clienteAEditar?.nombre)
        }

        val inputTelefono = EditText(this).apply {
            hint = "Teléfono (10 dígitos)"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            filters = arrayOf(android.text.InputFilter.LengthFilter(10))
            setText(clienteAEditar?.telefono)
        }

        val inputDireccion = EditText(this).apply {
            hint = "Dirección"
            setText(clienteAEditar?.direccion)
        }

        // CORRECCIÓN: Buscar descuento existente para este cliente
        var descuentoActualString = ""
        if (clienteAEditar != null) {
            val regla = listaReglas.find { it.clienteId == clienteAEditar.id && it.activo }
            if (regla != null) {
                descuentoActualString = regla.descuentoPorcentaje.toString()
            }
        }

        val inputDescuento = EditText(this).apply {
            hint = "Descuento fijo (%)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(descuentoActualString) // <--- Aquí rellenamos el campo
        }

        layout.addView(inputNombre)
        layout.addView(inputTelefono)
        layout.addView(inputDireccion)
        layout.addView(inputDescuento)
        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nombre = inputNombre.text.toString().trim()
            val tel = inputTelefono.text.toString().trim()
            val dir = inputDireccion.text.toString().trim()
            val descText = inputDescuento.text.toString().trim()
            val descuento = descText.toDoubleOrNull() ?: 0.0

            if (nombre.isEmpty()) {
                Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            } else {
                guardarCliente(nombre, tel, dir, descuento, clienteAEditar?.id)
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun guardarCliente(nombre: String, tel: String, dir: String, descuento: Double, id: Int?) {
        lifecycleScope.launch {
            try {
                val clienteData = ClienteIn(
                    nombre = nombre,
                    telefono = if (tel.isEmpty()) null else tel,
                    direccion = if (dir.isEmpty()) null else dir,
                    notas = null
                )

                // 1. Guardar/Actualizar Cliente
                val clienteGuardado: Cliente = if (id == null) {
                    RetrofitClient.instance.crearCliente(clienteData)
                } else {
                    RetrofitClient.instance.actualizarCliente(id, clienteData)
                }

                // 2. Manejar Descuento (Borrar viejo -> Crear nuevo)
                // Primero buscamos si ya tenía una regla vieja y la borramos para no duplicar
                val reglaVieja = listaReglas.find { it.clienteId == clienteGuardado.id }
                if (reglaVieja != null) {
                    try { RetrofitClient.instance.eliminarRegla(reglaVieja.id) } catch (e:Exception){}
                }

                // Si puso un descuento válido, creamos la nueva regla
                if (descuento > 0) {
                    val regla = ReglaDescuentoIn(
                        descripcion = "Desc. Cliente: $nombre",
                        descuentoPorcentaje = descuento,
                        clienteId = clienteGuardado.id,
                        activo = true
                    )
                    RetrofitClient.instance.crearRegla(regla)
                }

                Toast.makeText(this@ClientesActivity, "✅ Guardado correctamente", Toast.LENGTH_SHORT).show()
                cargarDatos() // Recargar todo
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error: ${e.message}")
                Toast.makeText(this@ClientesActivity, "❌ Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarCliente(cliente: Cliente) {
        lifecycleScope.launch {
            try {
                RetrofitClient.instance.eliminarCliente(cliente.id)
                cargarDatos()
                Toast.makeText(this@ClientesActivity, "🗑️ Cliente eliminado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ClientesActivity, "❌ Error al eliminar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarOpcionesCliente(view: View, cliente: Cliente) {
        val popup = androidx.appcompat.widget.PopupMenu(this, view)
        popup.menu.add("Editar")
        popup.menu.add("Eliminar")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Editar" -> mostrarDialogoNuevoCliente(cliente)
                "Eliminar" -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("¿Eliminar cliente?")
                        .setMessage("Esta acción no se puede deshacer.")
                        .setPositiveButton("Eliminar") { _, _ -> eliminarCliente(cliente) }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
            true
        }
        popup.show()
    }
}