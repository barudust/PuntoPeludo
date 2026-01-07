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
            finish() // Cierra esta pantalla
        }

        setupNavegacion()
    }

    private fun setupNavegacion() {
        // --- 🛒 NUEVA VENTA (AHORA SÍ FUNCIONA) ---
        findViewById<View>(R.id.cardNuevaVenta).setOnClickListener {
            // Conectamos con la pantalla que acabamos de crear
            val intent = Intent(this, VentaActivity::class.java)
            startActivity(intent)
        }

        // --- ➕ CREAR PRODUCTO (Ya funcionaba) ---
        findViewById<View>(R.id.cardCrearProducto).setOnClickListener {
            val intent = Intent(this, CrearProductoActivity::class.java)
            startActivity(intent)
        }


        findViewById<View>(R.id.cardInventario).setOnClickListener {
            startActivity(Intent(this, InventarioActivity::class.java))
        }

        // --- 🚛 SURTIR (Pendiente) ---
        findViewById<View>(R.id.cardSurtir).setOnClickListener {
            Toast.makeText(this, "Tu compañera debe crear SurtirActivity", Toast.LENGTH_SHORT).show()
        }

        // --- 💰 CAJA (Pendiente) ---
        findViewById<View>(R.id.cardCaja).setOnClickListener {
            Toast.makeText(this, "Tu compañera debe crear CajaActivity", Toast.LENGTH_SHORT).show()
        }

        // --- 👥 CLIENTES (Pendiente) ---
        findViewById<View>(R.id.cardClientes).setOnClickListener {
            Toast.makeText(this, "Falta el módulo de Clientes", Toast.LENGTH_SHORT).show()
        }

        // --- 📊 REPORTES (Pendiente) ---
        findViewById<View>(R.id.cardReportes).setOnClickListener {
            Toast.makeText(this, "Falta el módulo de Reportes", Toast.LENGTH_SHORT).show()
        }
    }
}