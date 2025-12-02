package com.example.puntopeludo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Para corutinas
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Referencias a tus campos (Asegúrate que los IDs coincidan con tu XML)
        val etUsuario = findViewById<EditText>(R.id.editTextUsuario)
        val etPassword = findViewById<EditText>(R.id.editTextPassword)
        val btnIngresar = findViewById<Button>(R.id.buttonLogin)

        btnIngresar.setOnClickListener {
            val usuario = etUsuario.text.toString()
            val password = etPassword.text.toString()

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Escribe usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lanzamos la petición en segundo plano
            lifecycleScope.launch {
                try {
                    // 1. Llamamos a la API
                    val respuesta = RetrofitClient.instance.login(usuario, password)

                    // 2. Si llegamos aquí, ¡ÉXITO!
                    val token = respuesta.accessToken
                    Toast.makeText(this@LoginActivity, "¡Login Correcto!", Toast.LENGTH_SHORT).show()

                    // TODO: Aquí guardaremos el Token en el futuro (SharedPreferences)

                    // 3. Ir al Dashboard
                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    finish() // Cierra el login para no volver atrás

                } catch (e: Exception) {
                    // 4. Si falla (401 o sin internet)
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, "Error: Credenciales inválidas o sin conexión", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}