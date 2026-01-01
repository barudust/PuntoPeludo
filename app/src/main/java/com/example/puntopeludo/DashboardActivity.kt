package com.example.puntopeludo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView // <-- IMPORTANTE: Cambiado de Button a MaterialCardView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Referencias a las nuevas TARJETAS
        val cardVenta = findViewById<MaterialCardView>(R.id.cardRegistrarVenta)
        val cardInventario = findViewById<MaterialCardView>(R.id.cardGestionInventario)
        val cardClientes = findViewById<MaterialCardView>(R.id.cardGestionClientes)

        // 1. Navegar a Inventario
        cardInventario.setOnClickListener {
            // Si 'InventarioActivity' aparece en rojo, verifica que el archivo InventarioActivity.kt exista
            val intent = Intent(this, InventarioActivity::class.java)
            startActivity(intent)
        }

        // 2. Navegar a Venta
        cardVenta.setOnClickListener {
             val intent = Intent(this, VentaActivity::class.java)
            startActivity(intent)
        }

        // 3. Navegar a Clientes
        cardClientes.setOnClickListener {

            val intent = Intent(this, ClientesActivity::class.java)
            startActivity(intent)
        }
    }
}
