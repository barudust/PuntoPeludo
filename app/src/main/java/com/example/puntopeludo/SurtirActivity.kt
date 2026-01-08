package com.example.puntopeludo

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// Agregamos el "AppCompatActivity()" para que herede las funciones de Android
class SurtirActivity : AppCompatActivity() {

    private var productoSeleccionado: Producto? = null
    private var listaProductos = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surtir)

        val auto = findViewById<AutoCompleteTextView>(R.id.autoCompleteSurtir)
        val etCantidad = findViewById<EditText>(R.id.etCantidadSurtir)
        val btn = findViewById<Button>(R.id.btnConfirmarSurtir)

        // 1. Cargar productos para el buscador
        lifecycleScope.launch {
            try {
                // Obtenemos los productos reales del servidor
                val productos = RetrofitClient.instance.getProductos()
                listaProductos.clear()
                listaProductos.addAll(productos)

                val nombres = listaProductos.map { it.nombre }
                val adapter = ArrayAdapter(this@SurtirActivity, android.R.layout.simple_dropdown_item_1line, nombres)
                auto.setAdapter(adapter)
            } catch (e: Exception) {
                Toast.makeText(this@SurtirActivity, "Error al cargar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        auto.setOnItemClickListener { parent, _, position, _ ->
            val nombre = parent.getItemAtPosition(position) as String
            productoSeleccionado = listaProductos.find { it.nombre == nombre }
        }

        // 2. Enviar a la API
        btn.setOnClickListener {
            val cantidad = etCantidad.text.toString().toDoubleOrNull()
            val producto = productoSeleccionado // Copia local segura

            if (producto != null && cantidad != null && cantidad > 0) {
                lifecycleScope.launch {
                    try {
                        val ingreso = IngresoInventarioIn(
                            producto_id = producto.id, //
                            sucursal_id = 1,
                            cantidad = cantidad, //
                            usuario_id = 1
                        )
                        RetrofitClient.instance.surtirProducto(ingreso) //
                        Toast.makeText(this@SurtirActivity, "Surtido exitoso", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@SurtirActivity, "Error de red", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Selecciona producto y cantidad", Toast.LENGTH_SHORT).show()
            }
        }


    }
}