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
    private var listaClientes = mutableListOf<Cliente>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes)

        // 1. Configurar el listado (RecyclerView)
        recyclerView = findViewById(R.id.recyclerViewClientes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // El adapter usa la función de eliminar definida abajo
        adapter = ClientesAdapter(listaClientes) { view, cliente ->
            mostrarOpcionesCliente(view, cliente)
        }
        recyclerView.adapter = adapter

        // 2. Botón Volver
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // 3. Botón Agregar
        findViewById<View>(R.id.fabAgregarCliente).setOnClickListener {
            mostrarDialogoNuevoCliente()
        }

        cargarClientes()
    }

    private fun cargarClientes() {
        lifecycleScope.launch {
            try {
                // Llama al GET /clientes/ de tu FastAPI
                val clientes = RetrofitClient.instance.getClientes()
                listaClientes.clear()
                listaClientes.addAll(clientes)
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
            // LIMITACIÓN FÍSICA: No deja escribir más de 10
            filters = arrayOf(android.text.InputFilter.LengthFilter(10))
            setText(clienteAEditar?.telefono)
        }

        val inputDireccion = EditText(this).apply {
            hint = "Dirección"
            setText(clienteAEditar?.direccion)
        }

        val inputDescuento = EditText(this).apply {
            hint = "Descuento fijo para este cliente (%)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            // Aquí podrías cargar el descuento actual si tuvieras la regla,
            // por ahora lo dejamos para asignar uno nuevo.
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
            } else if (tel.isNotEmpty() && tel.length < 10) {
                Toast.makeText(this, "El teléfono debe tener 10 dígitos", Toast.LENGTH_SHORT).show()
            } else {
                // Pasamos todos los datos, incluido el ID para EDITAR
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

                val clienteGuardado: Cliente = if (id == null) {
                    RetrofitClient.instance.crearCliente(clienteData)
                } else {
                    RetrofitClient.instance.actualizarCliente(id, clienteData)
                }

                // Si se ingresó un descuento, creamos la regla en el backend
                if (descuento > 0) {
                    // Cambia esto dentro de guardarCliente:
                    val regla = ReglaDescuentoIn(
                        descripcion = "Descuento especial: $nombre",
                        descuentoPorcentaje = descuento, // Antes decía descuento_porcentaje
                        clienteId = clienteGuardado.id,   // Antes decía cliente_id
                        activo = true
                    )
                    RetrofitClient.instance.crearRegla(regla)
                }

                Toast.makeText(this@ClientesActivity, "✅ Guardado correctamente", Toast.LENGTH_SHORT).show()
                cargarClientes() // Refrescar lista
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error: ${e.message}")
                Toast.makeText(this@ClientesActivity, "❌ Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun eliminarCliente(cliente: Cliente) {
        lifecycleScope.launch {
            try {
                // Llama al DELETE /clientes/{id} de tu FastAPI
                RetrofitClient.instance.eliminarCliente(cliente.id)
                cargarClientes()
                Toast.makeText(this@ClientesActivity, "🗑️ Cliente eliminado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ClientesActivity, "❌ No se pudo eliminar", Toast.LENGTH_SHORT).show()
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
                        .setPositiveButton("Eliminar") { _, _ ->
                            eliminarCliente(cliente)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
            true
        }
        popup.show()
    }
}