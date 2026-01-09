package com.example.puntopeludo

import android.content.Context
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

        // 1. Recuperar datos de SharedPreferences (Lo que guardamos en el Login)
        // Dentro de onCreate en DashboardActivity
        val prefs = getSharedPreferences("PuntoPeludoPrefs", MODE_PRIVATE)
        val nombreUser = prefs.getString("NOMBRE_USUARIO", "Usuario")
        val nombreSuc = prefs.getString("NOMBRE_SUCURSAL", "Sucursal")

        findViewById<TextView>(R.id.tvUserInfo).text = "¡Hola, $nombreUser!"
        findViewById<TextView>(R.id.tvSucursalInfo).text = nombreSuc

// Botón Cerrar Sesión corregido
        findViewById<View>(R.id.btnCerrarSesion).setOnClickListener {
            prefs.edit().clear().apply() // Borra todo
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupNavegacion()
    }

    private fun setupNavegacion() {
        // --- 🛒 NUEVA VENTA ---
        findViewById<View>(R.id.cardNuevaVenta).setOnClickListener {
            startActivity(Intent(this, VentaActivity::class.java))
        }

        // --- ➕ CREAR PRODUCTO ---
        findViewById<View>(R.id.cardCrearProducto).setOnClickListener {
            try {
                startActivity(Intent(this, CrearProductoActivity::class.java))
            } catch (e: Exception) {
                // Si aquí truena, es un error interno de CrearProductoActivity
                Toast.makeText(this, "Error al abrir: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // --- 📦 INVENTARIO ---
        findViewById<View>(R.id.cardInventario).setOnClickListener {
            startActivity(Intent(this, InventarioActivity::class.java))
        }

        // --- 👥 CLIENTES ---
        findViewById<View>(R.id.cardClientes).setOnClickListener {
            startActivity(Intent(this, ClientesActivity::class.java))
        }

        // --- 🚛 SURTIR ---
        findViewById<View>(R.id.cardSurtir).setOnClickListener {
            startActivity(Intent(this, SurtirActivity::class.java))
        }

        // --- 💰 CAJA ---
        findViewById<View>(R.id.cardCaja).setOnClickListener {
            startActivity(Intent(this, CajaActivity::class.java))
        }

        // --- 📊 REPORTES ---
        findViewById<View>(R.id.cardReportes).setOnClickListener {
            startActivity(Intent(this, ReportesActivity::class.java))
        }
    }
}