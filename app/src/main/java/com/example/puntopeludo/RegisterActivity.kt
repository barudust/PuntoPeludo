package com.example.puntopeludo

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Conecta con tu nuevo y mejorado XML

        // --- INICIO DE LAS CORRECCIONES ---

        // 1. Referencias actualizadas a los campos del nuevo layout
        val etFullName = findViewById<TextInputEditText>(R.id.editTextFullName)
        val autoCompleteSucursal = findViewById<AutoCompleteTextView>(R.id.autoCompleteSucursal) // Campo de sucursal
        val etPassword = findViewById<TextInputEditText>(R.id.editTextPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.editTextConfirmPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.buttonRegister)
        val tvLoginPrompt = findViewById<MaterialButton>(R.id.tvLoginPrompt) // Es un MaterialButton ahora
        val btnBack = findViewById<ImageButton>(R.id.btnBack) // Botón de regresar

        // 2. Lógica para poblar el menú desplegable de sucursales (¡IMPORTANTE!)
        // TODO: Reemplaza esta lista de ejemplo con los datos de tu API
        val sucursales = listOf("Sucursal Centro", "Sucursal Norte", "Sucursal Sur")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sucursales)
        autoCompleteSucursal.setAdapter(adapter)

        // 3. Listener para el botón de registrarse (lógica actualizada)
        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val sucursal = autoCompleteSucursal.text.toString().trim() // Obtenemos el texto de la sucursal
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validaciones básicas actualizadas
            if (fullName.isEmpty() || sucursal.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Lógica para registrar al usuario ---
            // Aquí iría tu llamada a Retrofit para el endpoint de registro,
            // enviando `fullName`, `sucursal` y `password`.

            Toast.makeText(this, "Registro exitoso para la $sucursal (simulado).", Toast.LENGTH_LONG).show()
            finish() // Cierra la pantalla de registro y vuelve al Login
        }

        // 4. Listeners para volver a la pantalla de Login
        tvLoginPrompt.setOnClickListener {
            finish() // Cierra esta actividad para volver a la anterior (Login)
        }

        btnBack.setOnClickListener {
            finish() // El botón de regresar también cierra la actividad
        }

        // --- FIN DE LAS CORRECCIONES ---
    }
}
