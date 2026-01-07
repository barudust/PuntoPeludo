package com.example.puntopeludo

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class InventarioActivity : AppCompatActivity() {

    private lateinit var adapter: InventarioAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView

    // Mantenemos el estado de los filtros
    private var configFiltros = FiltrosInventario()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        initViews()
    }

    private fun initViews() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<FloatingActionButton>(R.id.fabAgregar).setOnClickListener {
            startActivity(Intent(this, CrearProductoActivity::class.java))
        }

        // --- BOTÓN DE FILTROS POTENTE ---
        findViewById<ImageButton>(R.id.btnFiltros).setOnClickListener {
            mostrarPanelFiltros()
        }

        progressBar = findViewById(R.id.progressBar)
        searchView = findViewById(R.id.searchView)

        val rv = findViewById<RecyclerView>(R.id.recyclerViewInventario)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = InventarioAdapter { producto ->
            mostrarOpcionesProducto(producto)
        }
        rv.adapter = adapter

        // Buscador en tiempo real
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                configFiltros.textoBusqueda = newText ?: ""
                adapter.actualizarFiltros(configFiltros)
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val productos = RetrofitClient.instance.obtenerProductos()
                adapter.setProductos(productos)
                // Reaplicamos filtros por si había algo configurado
                adapter.actualizarFiltros(configFiltros)
            } catch (e: Exception) {
                Toast.makeText(this@InventarioActivity, "Error de red", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    // --- EL NUEVO PANEL DE FILTROS ---

    private fun mostrarPanelFiltros() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_filtros_inventario, null)

        // Referencias a los grupos
        val chipGroupEspecie = view.findViewById<ChipGroup>(R.id.chipGroupEspecie)
        val chipGroupCategoria = view.findViewById<ChipGroup>(R.id.chipGroupCategoria)
        val chipGroupOrden = view.findViewById<ChipGroup>(R.id.chipGroupOrden)

        // --- AQUÍ PODRÍAS PRE-SELECCIONAR LO QUE YA ESTABA MARCADO (Opcional) ---
        // Por simplicidad, el diálogo se abre limpio o por defecto cada vez,
        // pero mantiene los filtros en la variable 'configFiltros'.

        MaterialAlertDialogBuilder(this)
            .setTitle("Filtros Avanzados")
            .setView(view)
            .setPositiveButton("Aplicar") { _, _ ->

                // 1. Leer Especie
                val chipEspId = chipGroupEspecie.checkedChipId
                configFiltros.especie = if (chipEspId != -1) {
                    view.findViewById<Chip>(chipEspId).text.toString()
                } else "Todos"

                // 2. Leer Categoría
                val chipCatId = chipGroupCategoria.checkedChipId
                configFiltros.categoria = if (chipCatId != -1) {
                    view.findViewById<Chip>(chipCatId).text.toString()
                } else "Todas"

                // 3. Leer Estados (Checks múltiples)
                configFiltros.soloBajoStock = view.findViewById<Chip>(R.id.chipBajoStock).isChecked
                configFiltros.soloGranel = view.findViewById<Chip>(R.id.chipGranel).isChecked

                // 4. Leer Orden
                val chipOrdenId = chipGroupOrden.checkedChipId
                configFiltros.orden = when (chipOrdenId) {
                    R.id.chipPrecioMenor -> TipoOrden.PRECIO_ASC
                    R.id.chipPrecioMayor -> TipoOrden.PRECIO_DESC
                    else -> TipoOrden.DEFECTO
                }

                adapter.actualizarFiltros(configFiltros)
                Toast.makeText(this, "Filtros aplicados", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Limpiar") { _, _ ->
                configFiltros = FiltrosInventario() // Reset total
                configFiltros.textoBusqueda = searchView.query.toString()
                adapter.actualizarFiltros(configFiltros)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun mostrarOpcionesProducto(producto: ProductoResponse) {
        // ... (Igual que antes: Editar/Eliminar)
        val opciones = arrayOf("✏️ Ajustar Stock", "🗑️ Eliminar Producto")
        MaterialAlertDialogBuilder(this)
            .setTitle(producto.nombre)
            .setItems(opciones) { _, which ->
                if (which == 0) mostrarDialogoAjustarStock(producto)
                else confirmarEliminar(producto)
            }
            .show()
    }

    // Funciones auxiliares de stock y eliminar (simuladas por ahora)
    private fun mostrarDialogoAjustarStock(producto: ProductoResponse) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Stock actual: ${producto.stock}"

        // Un margen para que no se vea pegado
        val container = android.widget.FrameLayout(this)
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = 50
        params.rightMargin = 50
        input.layoutParams = params
        container.addView(input)

        MaterialAlertDialogBuilder(this)
            .setTitle("Ajustar Stock")
            .setMessage("Ingresa la nueva cantidad total:")
            .setView(container)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoStock = input.text.toString().toDoubleOrNull()

                if (nuevoStock != null) {
                    // --- CÓDIGO REAL ---
                    progressBar.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        try {
                            // Preparamos el sobrecito con el dato
                            val request = ActualizarStockRequest(nuevoStock)

                            // Enviamos al servidor
                            RetrofitClient.instance.actualizarStock(producto.id, request)

                            Toast.makeText(applicationContext, "✅ Stock actualizado", Toast.LENGTH_SHORT).show()
                            cargarDatos() // Recargamos para ver el cambio de color (rojo/verde)

                        } catch (e: Exception) {
                            Toast.makeText(applicationContext, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            progressBar.visibility = View.GONE
                        }
                    }
                    // -------------------
                } else {
                    Toast.makeText(this, "Número inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminar(producto: ProductoResponse) {
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Eliminar ${producto.nombre}?")
            .setMessage("Esta acción borrará el producto de la base de datos permanentemente.")
            .setPositiveButton("Eliminar") { _, _ ->

                // --- CÓDIGO REAL ---
                progressBar.visibility = View.VISIBLE
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.instance.eliminarProducto(producto.id)

                        if (response.isSuccessful) {
                            Toast.makeText(applicationContext, "✅ Producto eliminado", Toast.LENGTH_SHORT).show()
                            cargarDatos() // Recargamos la lista para que desaparezca
                        } else {
                            Toast.makeText(applicationContext, "❌ Error al eliminar: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        progressBar.visibility = View.GONE
                    }
                }
                // -------------------
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

}