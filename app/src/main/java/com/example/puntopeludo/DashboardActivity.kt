package com.example.puntopeludo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.button.MaterialButton

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // --- Encontramos los TRES botones por su ID ---
        val botonVenta = findViewById<MaterialButton>(R.id.buttonRegistrarVenta)
        val botonInventario = findViewById<MaterialButton>(R.id.buttonGestionInventario) // ¡OJO! Asegúrate que este ID sea correcto
        val botonClientes = findViewById<MaterialButton>(R.id.buttonGestionClientes)


        // --- Asignamos la acción al botón de Venta ---
        botonVenta.setOnClickListener {
            val intent = Intent(this, VentaActivity::class.java)
            startActivity(intent)
        }

        // --- Asignamos la acción al botón de Inventario ---
        botonInventario.setOnClickListener {
            val intent = Intent(this, InventarioActivity::class.java)
            startActivity(intent)
        }

        // --- (NUEVO) Asignamos la acción al botón de Clientes ---
        botonClientes.setOnClickListener {
            val intent = Intent(this, ClientesActivity::class.java)
            startActivity(intent)
        }
    }
}