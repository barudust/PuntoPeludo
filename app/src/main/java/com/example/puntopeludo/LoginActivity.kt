package com.example.puntopeludo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Carga el diseño XML 'activity_login' (¡Correcto!)
        setContentView(R.layout.activity_login)

        // 2. Encontramos el botón usando su ID
        val botonLogin = findViewById<Button>(R.id.buttonLogin)

        // 3. Le decimos qué hacer cuando le hagan clic
        botonLogin.setOnClickListener {

            // (Aquí, en el futuro, llamarías a tu API de login)

            // 4. Creamos una "Intención" para ir a la nueva pantalla
            val intent = Intent(this, DashboardActivity::class.java)

            // 5. Ejecutamos la intención
            startActivity(intent)
        }
    }
}