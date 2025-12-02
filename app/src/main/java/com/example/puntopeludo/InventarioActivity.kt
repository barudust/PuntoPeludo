package com.example.puntopeludo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch



class InventarioActivity : AppCompatActivity() {

    private lateinit var adapter: ProductoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        // 1. Configurar el RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInventario)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Iniciamos con lista vacía
        adapter = ProductoAdapter(emptyList())
        recyclerView.adapter = adapter

        // 2. Pedir datos al Backend
        cargarProductos()
    }

    private fun cargarProductos() {
        lifecycleScope.launch {
            try {
                // Llamada a la API
                val listaProductos = RetrofitClient.instance.obtenerProductos()
                val sucursalId = 1
                val inventario = RetrofitClient.instance.obtenerInventarioSucursal(sucursalId)

                adapter.actualizarLista(inventario)


                if (listaProductos.isEmpty()) {
                    Toast.makeText(this@InventarioActivity, "No hay productos registrados", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@InventarioActivity, "Error al cargar inventario: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}