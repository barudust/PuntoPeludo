package com.example.puntopeludo

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.launch

class InventarioActivity : AppCompatActivity() {

    private lateinit var adapter: InventarioAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView

    // Listas maestras para filtros y traducción (Mapas ID -> Nombre)
    private var listaProductosGlobal: List<ProductoResponse> = emptyList()
    private var mapaMarcas = mapOf<Int, String>()
    private var mapaCategorias = mapOf<Int, String>()
    private var mapaEspecies = mapOf<Int, String>()

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
            try { mostrarPanelFiltrosCompleto() }
            catch (e: Exception) { Toast.makeText(this, "Error filtros", Toast.LENGTH_SHORT).show() }
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
        cargarDatosCompletos()
    }

    private fun cargarDatosCompletos() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // 1. Cargar Catálogos para Nombres Reales
                val marcas = RetrofitClient.instance.getMarcas()
                mapaMarcas = marcas.associate { it.id to it.nombre }

                val categorias = RetrofitClient.instance.getCategorias()
                mapaCategorias = categorias.associate { it.id to it.nombre }

                val especies = RetrofitClient.instance.getEspecies()
                mapaEspecies = especies.associate { it.id to it.nombre }

                // 2. Cargar TODOS los productos (Activos y Eliminados)
                // Al pasar true, traemos la "Papelera" también para filtrarla localmente
                val productos = RetrofitClient.instance.obtenerProductos(mostrarInactivos = true)
                listaProductosGlobal = productos

                // 3. Configurar adaptador
                adapter.setDatos(productos, mapaMarcas, mapaCategorias, mapaEspecies)
                adapter.actualizarFiltros(configFiltros)

            } catch (e: Exception) {
                Log.e("INVENTARIO", "Error: ${e.message}")
                Toast.makeText(this@InventarioActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun mostrarPanelFiltrosCompleto() {
        val scrollView = ScrollView(this)
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 20, 40, 20) }
        scrollView.addView(container)

        // 1. FILTRO PAPELERA
        val switchInactivos = MaterialSwitch(this).apply {
            text = "🗑️ Ver Papelera (Eliminados)"
            textSize = 16f
            isChecked = configFiltros.mostrarInactivos
            setTextColor(if(isChecked) android.graphics.Color.RED else android.graphics.Color.BLACK)
        }
        container.addView(switchInactivos)

        // 2. FILTROS RÁPIDOS (Granel y Bajo Stock) - ¡RECUPERADOS!
        val switchGranel = MaterialSwitch(this).apply {
            text = "⚖️ Solo Venta a Granel"
            textSize = 16f
            isChecked = configFiltros.soloGranel
        }
        container.addView(switchGranel)

        val switchBajoStock = MaterialSwitch(this).apply {
            text = "⚠️ Solo Bajo Stock"
            textSize = 16f
            isChecked = configFiltros.soloBajoStock
        }
        container.addView(switchBajoStock)

        // 3. ORDENAMIENTO - ¡RECUPERADO!
        container.addView(crearTituloFiltro("Ordenar por:"))
        val cgOrden = crearChipGroup()
        container.addView(cgOrden)
        val opcionesOrden = listOf("Defecto", "Precio Mayor ⬆️", "Precio Menor ⬇️")
        val mapOrden = mapOf(
            "Defecto" to TipoOrden.DEFECTO,
            "Precio Mayor ⬆️" to TipoOrden.PRECIO_DESC,
            "Precio Menor ⬇️" to TipoOrden.PRECIO_ASC
        )
        // Selección actual
        val actualTxt = mapOrden.entries.find { it.value == configFiltros.orden }?.key ?: "Defecto"
        opcionesOrden.forEach { agregarChip(cgOrden, it, actualTxt) }


        // 4. MARCAS Y CATEGORÍAS (Con nombres reales)
        container.addView(crearTituloFiltro("Marca"))
        val cgMarca = crearChipGroup()
        container.addView(cgMarca)
        val nombresMarcas = mapaMarcas.values.sorted()
        agregarChip(cgMarca, "Todas", configFiltros.marca)
        nombresMarcas.forEach { agregarChip(cgMarca, it, configFiltros.marca) }

        container.addView(crearTituloFiltro("Categoría"))
        val cgCategoria = crearChipGroup()
        container.addView(cgCategoria)
        val nombresCat = mapaCategorias.values.sorted()
        agregarChip(cgCategoria, "Todas", configFiltros.categoria)
        nombresCat.forEach { agregarChip(cgCategoria, it, configFiltros.categoria) }

        MaterialAlertDialogBuilder(this)
            .setTitle("Filtros Avanzados")
            .setView(scrollView)
            .setPositiveButton("Aplicar") { _, _ ->
                // Actualizar configuración
                configFiltros.mostrarInactivos = switchInactivos.isChecked
                configFiltros.soloGranel = switchGranel.isChecked
                configFiltros.soloBajoStock = switchBajoStock.isChecked

                configFiltros.marca = obtenerTextoChip(cgMarca) ?: "Todas"
                configFiltros.categoria = obtenerTextoChip(cgCategoria) ?: "Todas"

                val ordenSeleccionado = obtenerTextoChip(cgOrden) ?: "Defecto"
                configFiltros.orden = mapOrden[ordenSeleccionado] ?: TipoOrden.DEFECTO

                adapter.actualizarFiltros(configFiltros)
            }
            .setNeutralButton("Limpiar Todo") { _, _ ->
                configFiltros = FiltrosInventario()
                adapter.actualizarFiltros(configFiltros)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarOpcionesProducto(producto: ProductoResponse) {
        if (!producto.activo) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Producto Eliminado")
                .setMessage("¿Deseas restaurar '${producto.nombre}' al inventario activo?")
                .setPositiveButton("♻️ Restaurar") { _, _ -> restaurarProducto(producto) }
                .setNegativeButton("Cancelar", null)
                .show()
            return
        }

        val opciones = arrayOf("✏️ Ajustar Stock", "🛠️ Editar Datos Completos", "🗑️ Eliminar Producto")
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
        val inputContenido = crearInput("Contenido Neto", producto.contenidoNeto?.toString() ?: "1.0", true, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val inputMinimo = crearInput("Stock Mínimo", producto.stockMinimo?.toString() ?: "5.0", true, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val inputPrecio = crearInput("Precio Base ($)", producto.precioBase.toString(), true, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)

        val precioGranelStr = if (producto.precioGranel != null && producto.precioGranel > 0) producto.precioGranel.toString() else ""
        val inputGranel = crearInput("Precio Granel/Kilo ($)", precioGranelStr, true, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)

        val switchGranel = MaterialSwitch(this).apply {
            text = "¿Se vende a granel?"
            isChecked = producto.esGranel
            textSize = 16f
            setPadding(0, 20, 0, 20)
        }

        inputGranel.visibility = if (producto.esGranel) View.VISIBLE else View.GONE
        switchGranel.setOnCheckedChangeListener { _, isChecked ->
            inputGranel.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        container.addView(inputNombre)
        container.addView(inputContenido)
        container.addView(inputMinimo)
        container.addView(inputPrecio)
        container.addView(switchGranel)
        container.addView(inputGranel)

        MaterialAlertDialogBuilder(this)
            .setTitle("Editar ${producto.nombre}")
            .setView(container)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = inputNombre.editText?.text.toString()
                val nuevoContenido = inputContenido.editText?.text.toString().toDoubleOrNull() ?: 1.0
                val nuevoMinimo = inputMinimo.editText?.text.toString().toDoubleOrNull() ?: 5.0
                val nuevoPrecio = inputPrecio.editText?.text.toString().toDoubleOrNull() ?: 0.0
                val nuevoPrecioGranel = inputGranel.editText?.text.toString().toDoubleOrNull()
                val esGranel = switchGranel.isChecked

                guardarEdicion(producto.id, nuevoNombre, nuevoContenido, nuevoMinimo, nuevoPrecio, nuevoPrecioGranel, esGranel)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarEdicion(
        id: Int,
        nombre: String,
        contenido: Double,
        minimo: Double,
        precio: Double,
        precioGranel: Double?,
        esGranel: Boolean
    ) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val request = EditarProductoRequest(
                    nombre = nombre,
                    contenidoNeto = contenido,
                    stockMinimo = minimo,
                    precioBase = precio,
                    precioGranel = precioGranel,
                    activo = true,
                    esGranel = esGranel
                )
                RetrofitClient.instance.editarProducto(id, request)
                Toast.makeText(this@InventarioActivity, "✅ Guardado", Toast.LENGTH_SHORT).show()
                cargarDatosCompletos()
            } catch (e: Exception) {
                Toast.makeText(this@InventarioActivity, "Error al guardar", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun restaurarProducto(producto: ProductoResponse) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val request = EditarProductoRequest(
                    nombre = producto.nombre,
                    contenidoNeto = producto.contenidoNeto ?: 1.0,
                    stockMinimo = producto.stockMinimo ?: 5.0,
                    precioBase = producto.precioBase,
                    precioGranel = producto.precioGranel,
                    activo = true,
                    esGranel = producto.esGranel
                )
                RetrofitClient.instance.editarProducto(producto.id, request)
                Toast.makeText(this@InventarioActivity, "✅ Restaurado", Toast.LENGTH_SHORT).show()
                cargarDatosCompletos()
            } catch (e: Exception) {
                Toast.makeText(this@InventarioActivity, "Error al restaurar", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    // --- Helpers UI ---
    private fun crearInput(hint: String, valor: String, editable: Boolean, tipo: Int = InputType.TYPE_CLASS_TEXT): TextInputLayout {
        val layout = TextInputLayout(this, null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox)
        layout.hint = hint
        layout.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, 20) }
        val et = TextInputEditText(layout.context).apply { setText(valor); inputType = tipo; isEnabled = editable }
        layout.addView(et)
        return layout
    }

    private fun agregarChip(grupo: ChipGroup, texto: String, filtroActual: String) {
        val chip = LayoutInflater.from(this).inflate(R.layout.item_chip_filtro, grupo, false) as Chip
        chip.text = texto
        chip.isChecked = (texto.equals(filtroActual, ignoreCase = true))
        grupo.addView(chip)
    }
    private fun crearChipGroup() = ChipGroup(this).apply { isSingleSelection = true }
    private fun crearTituloFiltro(t: String) = TextView(this).apply { text = t; textSize = 16f; setPadding(0, 30, 0, 10); setTypeface(null, android.graphics.Typeface.BOLD) }
    private fun obtenerTextoChip(grupo: ChipGroup): String? {
        val id = grupo.checkedChipId
        return if (id != -1) grupo.findViewById<Chip>(id).text.toString() else null
    }

    private fun mostrarDialogoAjustarStock(producto: ProductoResponse) {
        val input = EditText(this).apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; hint = "Stock actual: ${producto.stock}" }
        val container = LinearLayout(this).apply { setPadding(50,0,50,0); addView(input) }
        MaterialAlertDialogBuilder(this).setTitle("Ajustar Stock").setView(container)
            .setPositiveButton("Guardar") { _, _ ->
                val s = input.text.toString().toDoubleOrNull()
                if (s != null) {
                    progressBar.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        try {
                            RetrofitClient.instance.actualizarStock(producto.id, ActualizarStockRequest(s))
                            Toast.makeText(applicationContext, "Stock actualizado", Toast.LENGTH_SHORT).show()
                            cargarDatosCompletos()
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
                    cargarDatosCompletos()
                } catch(e:Exception){} finally { progressBar.visibility = View.GONE }
            }
        }.setNegativeButton("No", null).show()
    }
}