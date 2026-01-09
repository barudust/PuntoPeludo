package com.example.puntopeludo

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Conecta con el XML que creamos

        // Referencias a todos los campos del layout de registro
        val etFullName = findViewById<TextInputEditText>(R.id.editTextFullName)
        val etEmail = findViewById<TextInputEditText>(R.id.editTextEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.editTextPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.editTextConfirmPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.buttonRegister)
        val tvLoginPrompt = findViewById<TextView>(R.id.tvLoginPrompt)

        // Listener para el botón de registrarse
        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validaciones básicas
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Lógica para registrar al usuario ---
            // Aquí iría tu llamada a Retrofit para el endpoint de registro
            // Por ahora, mostraremos un mensaje de éxito simulado.

            Toast.makeText(this, "Registro exitoso (simulado). Ahora inicia sesión.", Toast.LENGTH_LONG).show()
            finish() // Cierra la pantalla de registro y vuelve al Login
        }

        // Listener para el texto que lleva de vuelta al Login
        tvLoginPrompt.setOnClickListener {
            // Cierra la actividad de registro para volver a la de login
            finish()
        }
    }
}

