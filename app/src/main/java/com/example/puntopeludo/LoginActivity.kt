package com.example.puntopeludo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView // Importante para el TextView
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

        // CAMBIO: Referencias actualizadas a los nuevos componentes
        val etUsuario = findViewById<TextInputEditText>(R.id.editTextUsuario)
        val etPassword = findViewById<TextInputEditText>(R.id.editTextPassword)
        val btnIngresar = findViewById<MaterialButton>(R.id.buttonLogin)
        val tvRegisterPrompt = findViewById<TextView>(R.id.tvRegisterPrompt) // AÑADIDO

        btnIngresar.setOnClickListener {
            val usuario = etUsuario.text.toString()
            val password = etPassword.text.toString()

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Escribe correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val respuesta = RetrofitClient.instance.login(usuario, password)
                    val prefs = getSharedPreferences("PuntoPeludoPrefs", MODE_PRIVATE)
                    with(prefs.edit()) {
                        putInt("ID_USUARIO_SESION", respuesta.usuarioId)
                        putInt("ID_SUCURSAL_SESION", respuesta.sucursalId)
                        putString("TOKEN_SESION", respuesta.accessToken)
                        apply() // Usa apply() en lugar de commit() para operación asíncrona
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

        // AÑADIDO: Listener para navegar a la pantalla de registro
        tvRegisterPrompt.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
