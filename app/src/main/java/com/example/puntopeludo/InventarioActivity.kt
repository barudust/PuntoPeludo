package com.example.puntopeludo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class InventarioActivity : AppCompatActivity() {

    private lateinit var adapter: ProductoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        // 1. Configurar Recycler
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInventario)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductoAdapter(emptyList())
        recyclerView.adapter = adapter

        // 2. Botón para Agregar Producto (+)
        val fab = findViewById<FloatingActionButton>(R.id.fabAgregar)
        fab.setOnClickListener {
            val intent = Intent(this, CrearProductoActivity::class.java)
            startActivity(intent)
        }

        // 3. Cargar datos
        cargarInventario()
    }

    override fun onResume() {
        super.onResume()
        // Recargamos la lista al volver de "Crear Producto"
        cargarInventario()
    }

    private fun cargarInventario() {
        lifecycleScope.launch {
            try {
                // Usamos la sucursal 1 por defecto para pruebas
                val lista = RetrofitClient.instance.obtenerInventarioSucursal(1)
                adapter.actualizarLista(lista)

                if (lista.isEmpty()) {
                    Toast.makeText(this@InventarioActivity, "Inventario vacío", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@InventarioActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}