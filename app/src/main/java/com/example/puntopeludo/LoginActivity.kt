package com.example.puntopeludo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Referencias actualizadas para que coincidan con el nuevo XML
        val etUsername = findViewById<TextInputEditText>(R.id.editTextUsername) // ID Corregido
        val etPassword = findViewById<TextInputEditText>(R.id.editTextPassword) // ID Corregido
        val btnLogin = findViewById<MaterialButton>(R.id.buttonLogin)
        val btnGoToRegister = findViewById<MaterialButton>(R.id.btnGoToRegister) // ID del nuevo botón

        // 2. Listener del botón de "Iniciar Sesión" (lógica de login)
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    // La lógica de Retrofit y SharedPreferences se mantiene igual
                    val respuesta = RetrofitClient.instance.login(username, password)
                    val prefs = getSharedPreferences("PuntoPeludoPrefs", MODE_PRIVATE)
                    with(prefs.edit()) {
                        putInt("ID_USUARIO_SESION", respuesta.usuarioId)
                        putInt("ID_SUCURSAL_SESION", respuesta.sucursalId)
                        putString("TOKEN_SESION", respuesta.accessToken)
                        putString("NOMBRE_USUARIO", respuesta.nombre)
                        putString("NOMBRE_SUCURSAL", respuesta.sucursalNombre)
                        apply()
                    }

                    Toast.makeText(this@LoginActivity, "¡Bienvenido!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, "Error: Credenciales inválidas o sin conexión", Toast.LENGTH_LONG).show()
                }
            }
        }

        // 3. Listener para el botón "Crear una cuenta" que navega a la pantalla de registro
        btnGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
