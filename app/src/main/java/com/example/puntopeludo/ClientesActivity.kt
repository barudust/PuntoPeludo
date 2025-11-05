package com.example.puntopeludo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ClientesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simplemente cargamos el diseño XML que hicimos
        setContentView(R.layout.activity_clientes)
    }
}