package com.example.puntopeludo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class InventarioActivity : AppCompatActivity() {

    private lateinit var adapter: InventarioAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView

    private var listaProductosGlobal: List<ProductoResponse> = emptyList()
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

        findViewById<ImageButton>(R.id.btnFiltros).setOnClickListener {
            try {
                mostrarPanelFiltrosCompleto()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al abrir filtros: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        progressBar = findViewById(R.id.progressBar)
        searchView = findViewById(R.id.searchView)
        val rv = findViewById<RecyclerView>(R.id.recyclerViewInventario)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = InventarioAdapter { producto ->
            mostrarOpcionesProducto(producto)
        }
        rv.adapter = adapter

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
                listaProductosGlobal = productos
                adapter.setProductos(productos)
                adapter.actualizarFiltros(configFiltros)
            } catch (e: Exception) {
                Toast.makeText(this@InventarioActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    // --- PANEL DE FILTROS COMPLETO ---
    private fun mostrarPanelFiltrosCompleto() {
        val scrollView = ScrollView(this)
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(40, 20, 40, 20)
        scrollView.addView(container)

        // 1. MARCAS (Dinámico)
        container.addView(crearTituloFiltro("Marca (ID)"))
        val cgMarca = crearChipGroup()
        container.addView(cgMarca)
        val marcas = listaProductosGlobal.map { if (it.marcaId != null) "ID: ${it.marcaId}" else "Sin Marca" }.distinct().sorted()
        agregarChip(cgMarca, "Todas", configFiltros.marca)
        marcas.forEach { agregarChip(cgMarca, it, configFiltros.marca) }

        // 2. CATEGORÍAS (Usando Tipo Producto)
        container.addView(crearTituloFiltro("Categoría (Tipo)"))
        val cgCategoria = crearChipGroup()
        container.addView(cgCategoria)
        val categorias = listaProductosGlobal.map { it.tipoProducto ?: "Otros" }.distinct().sorted()
        agregarChip(cgCategoria, "Todas", configFiltros.categoria)
        categorias.forEach { agregarChip(cgCategoria, it, configFiltros.categoria) }

        // 3. ESPECIES (Dinámico)
        container.addView(crearTituloFiltro("Especie (ID)"))
        val cgEspecie = crearChipGroup()
        container.addView(cgEspecie)
        val especies = listaProductosGlobal.map { if (it.especieId != null) "ID: ${it.especieId}" else "Sin Especie" }.distinct().sorted()
        agregarChip(cgEspecie, "Todos", configFiltros.especie)
        especies.forEach { agregarChip(cgEspecie, it, configFiltros.especie) }

        // 4. CHECKBOXES (Opciones)
        container.addView(crearTituloFiltro("Opciones"))
        val cbStock = CheckBox(this).apply { text = "Solo Bajo Stock"; isChecked = configFiltros.soloBajoStock }
        val cbGranel = CheckBox(this).apply { text = "Solo a Granel"; isChecked = configFiltros.soloGranel }
        container.addView(cbStock)
        container.addView(cbGranel)

        // 5. ORDENAMIENTO
        container.addView(crearTituloFiltro("Ordenar"))
        val cgOrden = crearChipGroup()
        container.addView(cgOrden)
        agregarChip(cgOrden, "Normal", if(configFiltros.orden == TipoOrden.DEFECTO) "Normal" else "")
        agregarChip(cgOrden, "Precio: Menor a Mayor", if(configFiltros.orden == TipoOrden.PRECIO_ASC) "Precio: Menor a Mayor" else "")
        agregarChip(cgOrden, "Precio: Mayor a Menor", if(configFiltros.orden == TipoOrden.PRECIO_DESC) "Precio: Mayor a Menor" else "")

        MaterialAlertDialogBuilder(this)
            .setTitle("Filtros")
            .setView(scrollView)
            .setPositiveButton("Aplicar") { _, _ ->
                configFiltros.marca = obtenerTextoChip(cgMarca) ?: "Todas"
                configFiltros.categoria = obtenerTextoChip(cgCategoria) ?: "Todas"
                configFiltros.especie = obtenerTextoChip(cgEspecie) ?: "Todos"
                configFiltros.soloBajoStock = cbStock.isChecked
                configFiltros.soloGranel = cbGranel.isChecked

                val ordenTxt = obtenerTextoChip(cgOrden) ?: "Normal"
                configFiltros.orden = when {
                    ordenTxt.contains("Menor") -> TipoOrden.PRECIO_ASC
                    ordenTxt.contains("Mayor") -> TipoOrden.PRECIO_DESC
                    else -> TipoOrden.DEFECTO
                }
                adapter.actualizarFiltros(configFiltros)
            }
            .setNeutralButton("Limpiar") { _, _ ->
                configFiltros = FiltrosInventario()
                adapter.actualizarFiltros(configFiltros)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- HELPERS PARA CHIPS (Evitan el crash) ---
    private fun agregarChip(grupo: ChipGroup, texto: String, filtroActual: String) {
        // Inflamos el XML que creaste en el paso 1
        val chip = LayoutInflater.from(this).inflate(R.layout.item_chip_filtro, grupo, false) as Chip
        chip.text = texto
        chip.isChecked = (texto.equals(filtroActual, ignoreCase = true))
        grupo.addView(chip)
    }

    private fun crearChipGroup() = ChipGroup(this).apply { isSingleSelection = true }
    private fun crearTituloFiltro(t: String) = TextView(this).apply {
        text = t; textSize = 16f; setPadding(0, 30, 0, 10); setTypeface(null, android.graphics.Typeface.BOLD)
    }
    private fun obtenerTextoChip(grupo: ChipGroup): String? {
        val id = grupo.checkedChipId
        if (id != -1) return grupo.findViewById<Chip>(id).text.toString()
        return null
    }

    // --- GESTIÓN DE PRODUCTO ---
    private fun mostrarOpcionesProducto(producto: ProductoResponse) {
        val opciones = arrayOf("✏️ Ajustar Stock Rápido", "🛠️ Editar Datos", "🗑️ Eliminar Producto")
        MaterialAlertDialogBuilder(this)
            .setTitle(producto.nombre)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarDialogoAjustarStock(producto)
                    1 -> mostrarDialogoEditarDatos(producto)
                    2 -> confirmarEliminar(producto)
                }
            }
            .show()
    }

    private fun mostrarDialogoEditarDatos(producto: ProductoResponse) {
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 40, 50, 20) }

        val inputNombre = crearInput("Nombre", producto.nombre, true)
        val inputContenido = crearInput("Contenido Neto", producto.contenidoNeto?.toString() ?: "0", true, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val inputMinimo = crearInput("Stock Mínimo (Alerta)", producto.stockMinimo?.toString() ?: "5", true, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val inputPrecio = crearInput("Precio Base ($)", producto.precioBase.toString(), true, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)

        container.addView(inputNombre)
        container.addView(inputContenido)
        container.addView(inputMinimo)
        container.addView(inputPrecio)

        MaterialAlertDialogBuilder(this)
            .setTitle("Editar ${producto.nombre}")
            .setView(container)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.editText?.text.toString()
                val contenido = inputContenido.editText?.text.toString().toDoubleOrNull() ?: 0.0
                val minimo = inputMinimo.editText?.text.toString().toDoubleOrNull() ?: 5.0
                val precio = inputPrecio.editText?.text.toString().toDoubleOrNull() ?: 0.0

                if (nombre.isNotEmpty()) {
                    guardarEdicion(producto.id, nombre, contenido, minimo, precio)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun crearInput(hint: String, valor: String, editable: Boolean, tipo: Int = InputType.TYPE_CLASS_TEXT): TextInputLayout {
        val layout = TextInputLayout(this, null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox)
        layout.hint = hint
        layout.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, 20) }
        val et = TextInputEditText(layout.context).apply { setText(valor); inputType = tipo; isEnabled = editable }
        layout.addView(et)
        return layout
    }

    private fun guardarEdicion(id: Int, nombre: String, contenido: Double, minimo: Double, precio: Double) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val request = EditarProductoRequest(nombre, contenido, minimo, precio)
                // Llamamos a la API (Recuerda que en ApiService debe tener el slash final)
                val response = RetrofitClient.instance.editarProducto(id, request)

                if(response.isSuccessful || response.code() == 200) {
                    Toast.makeText(applicationContext, "✅ Guardado correctamente", Toast.LENGTH_SHORT).show()
                    cargarDatos()
                } else {
                    Toast.makeText(applicationContext, "Error Servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Fallo conexión: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun mostrarDialogoAjustarStock(producto: ProductoResponse) {
        val input = EditText(this).apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; hint = "Stock actual: ${producto.stock}" }
        val container = LinearLayout(this).apply { setPadding(50,0,50,0); addView(input) } // Cambié FrameLayout a LinearLayout por simplicidad
        MaterialAlertDialogBuilder(this).setTitle("Ajustar Stock").setView(container)
            .setPositiveButton("Guardar") { _, _ ->
                val s = input.text.toString().toDoubleOrNull()
                if (s != null) {
                    progressBar.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        try {
                            RetrofitClient.instance.actualizarStock(producto.id, ActualizarStockRequest(s))
                            Toast.makeText(applicationContext, "Stock actualizado", Toast.LENGTH_SHORT).show()
                            cargarDatos()
                        } catch(e:Exception){Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()}
                        finally { progressBar.visibility = View.GONE }
                    }
                }
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun confirmarEliminar(producto: ProductoResponse) {
        MaterialAlertDialogBuilder(this).setTitle("¿Eliminar?").setPositiveButton("Sí") { _, _ ->
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    RetrofitClient.instance.eliminarProducto(producto.id)
                    cargarDatos()
                } catch(e:Exception){} finally { progressBar.visibility = View.GONE }
            }
        }.setNegativeButton("No", null).show()
    }
}