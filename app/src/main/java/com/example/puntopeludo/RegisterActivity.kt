package com.example.puntopeludo

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    // Variables para manejar la selección de sucursal
    private var listaSucursales = listOf<Sucursal>()
    private var sucursalSeleccionadaId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        inicializarVistas()
        cargarSucursales()
    }

    private fun inicializarVistas() {
        val etNombre = findViewById<TextInputEditText>(R.id.editTextFullName)
        val etPassword = findViewById<TextInputEditText>(R.id.editTextPassword)
        val etConfirmPass = findViewById<TextInputEditText>(R.id.editTextConfirmPassword)
        val spSucursal = findViewById<AutoCompleteTextView>(R.id.autoCompleteSucursal)

        // Botón Volver
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.tvLoginPrompt).setOnClickListener { finish() }

        // Configuración del Dropdown de Sucursal
        spSucursal.inputType = 0 // Para que no salga el teclado
        spSucursal.setOnClickListener { spSucursal.showDropDown() }
        spSucursal.setOnItemClickListener { parent, _, position, _ ->
            val nombreSucursal = parent.getItemAtPosition(position) as String
            // Buscamos el ID real basado en el nombre seleccionado
            sucursalSeleccionadaId = listaSucursales.find { it.nombre == nombreSucursal }?.id
        }

        // Botón Registrar
        findViewById<MaterialButton>(R.id.buttonRegister).setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val confirm = etConfirmPass.text.toString().trim()

            // Validaciones
            if (nombre.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (sucursalSeleccionadaId == null) {
                Toast.makeText(this, "Debes seleccionar una sucursal existente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Todo listo, enviamos al servidor
            registrarUsuario(nombre, pass, sucursalSeleccionadaId!!)
        }
    }

    private fun cargarSucursales() {
        lifecycleScope.launch {
            try {
                // Descargamos la lista REAL de sucursales
                listaSucursales = RetrofitClient.instance.getSucursales()

                // Extraemos solo los nombres para el adaptador visual
                val nombres = listaSucursales.map { it.nombre }

                val adapter = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_dropdown_item_1line, nombres)
                findViewById<AutoCompleteTextView>(R.id.autoCompleteSucursal).setAdapter(adapter)

            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error al cargar sucursales: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun registrarUsuario(nombre: String, pass: String, sucursalId: Int) {
        lifecycleScope.launch {
            try {
                val nuevoUsuario = UsuarioRegistroIn(
                    nombre = nombre,
                    contrasena = pass,
                    sucursalId = sucursalId,
                    rol = "Vendedor" // Por defecto, o puedes agregar un selector si quieres
                )

                RetrofitClient.instance.registrarUsuario(nuevoUsuario)

                Toast.makeText(this@RegisterActivity, "¡Cuenta creada! Inicia sesión.", Toast.LENGTH_LONG).show()
                finish() // Cierra el registro y vuelve al Login

            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error al registrar: Verifica tu conexión", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}