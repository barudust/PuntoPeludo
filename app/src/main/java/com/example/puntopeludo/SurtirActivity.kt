package com.example.puntopeludo

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SurtirActivity : AppCompatActivity() {

    private var productoSeleccionado: Producto? = null
    private var listaProductos = mutableListOf<Producto>()
    private var listaParaSurtir = mutableListOf<ProductoSurtido>()
    private lateinit var adapterSurtir: SurtirAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surtir)

        val auto = findViewById<AutoCompleteTextView>(R.id.autoCompleteSurtir)
        val etCantidad = findViewById<EditText>(R.id.etCantidadSurtir)
        val rvLista = findViewById<RecyclerView>(R.id.rvSurtirLista)
        val btnAgregar = findViewById<Button>(R.id.btnConfirmarSurtir)
        val btnConfirmarTodo = findViewById<Button>(R.id.btnConfirmarSurtirTodo)

        // 1. Configurar RecyclerView
        adapterSurtir = SurtirAdapter(listaParaSurtir,
            onEliminar = { posicion ->
                listaParaSurtir.removeAt(posicion)
                adapterSurtir.notifyItemRemoved(posicion)
            },
            onEditar = { posicion, item ->
                mostrarDialogoEditarCantidad(posicion, item)
            }
        )
        rvLista.adapter = adapterSurtir
        rvLista.layoutManager = LinearLayoutManager(this)

        // 2. Cargar productos para el buscador
        lifecycleScope.launch {
            try {
                val productos = RetrofitClient.instance.getProductos()
                listaProductos.clear()
                listaProductos.addAll(productos)

                val nombres = listaProductos.map { it.nombre }
                val adapterAuto = ArrayAdapter(this@SurtirActivity, android.R.layout.simple_dropdown_item_1line, nombres)
                auto.setAdapter(adapterAuto)
            } catch (e: Exception) {
                Toast.makeText(this@SurtirActivity, "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
        }

        auto.setOnItemClickListener { parent, _, position, _ ->
            val nombre = parent.getItemAtPosition(position) as String
            productoSeleccionado = listaProductos.find { it.nombre == nombre }
        }

        // AGREGAR A LA LISTA LOCAL (Botón Azul)
        btnAgregar.setOnClickListener {
            val prod = productoSeleccionado
            val cant = etCantidad.text.toString().toDoubleOrNull()

            if (prod != null && cant != null && cant > 0) {
                listaParaSurtir.add(ProductoSurtido(prod.id, prod.nombre, cant))
                adapterSurtir.notifyDataSetChanged()

                auto.text.clear()
                etCantidad.text.clear()
                productoSeleccionado = null
                Toast.makeText(this, "Agregado a la lista", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Selecciona producto y cantidad válida", Toast.LENGTH_SHORT).show()
            }
        }

        // ENVIAR TODO AL SERVIDOR (Botón Verde)
        btnConfirmarTodo.setOnClickListener {
            if (listaParaSurtir.isEmpty()) {
                Toast.makeText(this, "La lista está vacía", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    listaParaSurtir.forEach { item ->
                        val ingreso = IngresoInventarioIn(
                            producto_id = item.id,
                            sucursal_id = 1,
                            cantidad = item.cantidad,
                            usuario_id = 1
                        )
                        RetrofitClient.instance.surtirProducto(ingreso)
                    }
                    Toast.makeText(this@SurtirActivity, "✅ Todo el cargamento registrado", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@SurtirActivity, "Error al procesar la carga", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // FUNCIÓN MOVIDA FUERA DE ONCREATE
    private fun mostrarDialogoEditarCantidad(posicion: Int, item: ProductoSurtido) {
        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(item.cantidad.toString())
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Editar cantidad")
            .setMessage(item.nombre)
            .setView(input)
            .setPositiveButton("Actualizar") { _, _ ->
                val nuevaCant = input.text.toString().toDoubleOrNull() ?: item.cantidad
                listaParaSurtir[posicion] = item.copy(cantidad = nuevaCant)
                adapterSurtir.notifyItemChanged(posicion)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}