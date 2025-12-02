package com.example.puntopeludo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnVenta = findViewById<Button>(R.id.btnRegistrarVenta) // Asegúrate que el ID en el XML sea este
        val btnInventario = findViewById<Button>(R.id.btnGestionInventario) // Y este
        val btnClientes = findViewById<Button>(R.id.btnGestionClientes) // Y este

        // 1. Navegar a Inventario
        btnInventario.setOnClickListener {
            // Si 'InventarioActivity' aparece en rojo, verifica que el archivo InventarioActivity.kt exista
            val intent = Intent(this, InventarioActivity::class.java)
            startActivity(intent)
        }

        // 2. Navegar a Venta (Aún no la creamos, pero dejamos el espacio)
        btnVenta.setOnClickListener {
            // val intent = Intent(this, VentaActivity::class.java)
            // startActivity(intent)
        }

        // 3. Navegar a Clientes (Aún no la creamos)
        btnClientes.setOnClickListener {
            // val intent = Intent(this, ClientesActivity::class.java)
            // startActivity(intent)
        }
    }
}