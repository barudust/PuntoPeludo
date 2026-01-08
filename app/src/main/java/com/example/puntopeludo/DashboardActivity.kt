package com.example.puntopeludo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 1. Configurar textos de bienvenida
        val tvUsuario = findViewById<TextView>(R.id.tvUserInfo)
        tvUsuario.text = "¡Hola, Baruc!"

        // 2. Botón Cerrar Sesión
        findViewById<View>(R.id.btnCerrarSesion).setOnClickListener {
            finish()
        }

        // 3. Llamar a la navegación (Asegúrate de no repetir listeners aquí)
        setupNavegacion()
    }

    private fun setupNavegacion() {
        // --- 🛒 NUEVA VENTA ---
        findViewById<View>(R.id.cardNuevaVenta).setOnClickListener {
            startActivity(Intent(this, VentaActivity::class.java))
        }

        // --- ➕ CREAR PRODUCTO ---
        findViewById<View>(R.id.cardCrearProducto).setOnClickListener {
            startActivity(Intent(this, CrearProductoActivity::class.java))
        }

        // --- 📦 INVENTARIO ---
        findViewById<View>(R.id.cardInventario).setOnClickListener {
            startActivity(Intent(this, InventarioActivity::class.java))
        }

        // --- 👥 CLIENTES (CORREGIDO: Ya no manda Toast, ahora abre la Activity) ---
        findViewById<View>(R.id.cardClientes).setOnClickListener {
            startActivity(Intent(this, ClientesActivity::class.java))
        }

        // --- 🚛 SURTIR ---
        findViewById<View>(R.id.cardSurtir).setOnClickListener {
            val intent = Intent(this, SurtirActivity::class.java)
            startActivity(intent)
        }

        // --- 💰 CAJA ---
        findViewById<View>(R.id.cardCaja).setOnClickListener {
            val intent = Intent(this, CajaActivity::class.java)
            startActivity(intent)
        }

        // --- 📊 REPORTES ---
        findViewById<View>(R.id.cardReportes).setOnClickListener {
            Toast.makeText(this, "Módulo Reportes en desarrollo", Toast.LENGTH_SHORT).show()
        }
    }
}