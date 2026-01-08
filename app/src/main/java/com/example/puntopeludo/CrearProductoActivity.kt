package com.example.puntopeludo

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log

class CrearProductoActivity : AppCompatActivity() {

    // Inputs Textos
    private lateinit var etNombre: TextInputEditText
    private lateinit var etPrecioBase: TextInputEditText
    private lateinit var etContenido: TextInputEditText
    private lateinit var etStockMinimo: TextInputEditText

    // Dropdowns (Listas)
    private lateinit var spTipo: AutoCompleteTextView
    private lateinit var spMarca: AutoCompleteTextView
    private lateinit var spCategoria: AutoCompleteTextView
    private lateinit var spEspecie: AutoCompleteTextView
    private lateinit var spEtapa: AutoCompleteTextView
    private lateinit var spUnidad: AutoCompleteTextView

    // Botones de Edición (Lápiz)
    private lateinit var btnEditTipo: ImageButton
    private lateinit var btnEditMarca: ImageButton
    private lateinit var btnEditCategoria: ImageButton
    private lateinit var btnEditEspecie: ImageButton
    private lateinit var btnEditEtapa: ImageButton

    // Contenedores (Rows completas para ocultar/mostrar)
    private lateinit var rowMarca: LinearLayout
    private lateinit var rowCategoria: LinearLayout
    private lateinit var rowEspecie: LinearLayout
    private lateinit var rowEtapa: LinearLayout

    // Chips
    private lateinit var chipMarca: Chip
    private lateinit var chipCategoria: Chip
    private lateinit var chipEspecie: Chip
    private lateinit var chipEtapa: Chip

    // Granel y Guardar
    private lateinit var switchGranel: MaterialSwitch
    private lateinit var layoutGranel: TextInputLayout
    private lateinit var etPrecioGranel: TextInputEditText
    private lateinit var btnGuardar: Button

    // DATOS EN MEMORIA (Listas Editables)
    private var listaTipos = mutableListOf<String>()
    private var listaMarcas = mutableListOf<String>()
    private var listaCategorias = mutableListOf<String>()
    private var listaEspecies = mutableListOf<String>()
    private var listaEtapas = mutableListOf<String>()

    // IDs seleccionados
    private var idMarcaSeleccionada: Int? = null
    private var idCategoriaSeleccionada: Int? = null
    private var idEspecieSeleccionada: Int? = null
    private var idEtapaSeleccionada: Int? = null
    private var idTipoSeleccionado: Int? = null

    // Listas de objetos reales del servidor
    private var listaTiposObj = mutableListOf<TipoProducto>()
    private var listaMarcasObj = mutableListOf<Marca>()
    private var listaCategoriasObj = mutableListOf<Categoria>()
    private var listaEspeciesObj = mutableListOf<Especie>()
    private var listaEtapasObj = mutableListOf<Etapa>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. PRIMERO inflar la vista
        setContentView(R.layout.activity_crear_producto)

        // 2. SEGUNDO inicializar las vistas con findViewById
        inicializarVistas()

        // 3. TERCERO configurar los listeners y lógica
        configurarListenersEspeciales()
        configurarChips()
        configurarGranel()

        // Configurar todas las listas con poder de edición (locales)
        configurarDropdownConEdicion(spTipo, btnEditTipo, listaTipos, "Tipos de Producto")
        configurarDropdownConEdicion(spMarca, btnEditMarca, listaMarcas, "Marcas")
        configurarDropdownConEdicion(spCategoria, btnEditCategoria, listaCategorias, "Categorías")
        configurarDropdownConEdicion(spEspecie, btnEditEspecie, listaEspecies, "Especies")
        configurarDropdownConEdicion(spEtapa, btnEditEtapa, listaEtapas, "Etapas")

        // 4. CUARTO cargar los datos desde la API
        cargarDatosDesdeAPI()

        btnGuardar.setOnClickListener { guardarProducto() }
    }

    private fun inicializarVistas() {
        etNombre = findViewById(R.id.etNombre)
        etPrecioBase = findViewById(R.id.etPrecioBase)
        etContenido = findViewById(R.id.etContenido)
        etStockMinimo = findViewById(R.id.etStockMinimo)

        spTipo = findViewById(R.id.spTipo)
        spMarca = findViewById(R.id.spMarca)
        spCategoria = findViewById(R.id.spCategoria)
        spEspecie = findViewById(R.id.spEspecie)
        spEtapa = findViewById(R.id.spEtapa)
        spUnidad = findViewById(R.id.spUnidad)

        btnEditTipo = findViewById(R.id.btnEditTipo)
        btnEditMarca = findViewById(R.id.btnEditMarca)
        btnEditCategoria = findViewById(R.id.btnEditCategoria)
        btnEditEspecie = findViewById(R.id.btnEditEspecie)
        btnEditEtapa = findViewById(R.id.btnEditEtapa)

        rowMarca = findViewById(R.id.rowMarca)
        rowCategoria = findViewById(R.id.rowCategoria)
        rowEspecie = findViewById(R.id.rowEspecie)
        rowEtapa = findViewById(R.id.rowEtapa)

        chipMarca = findViewById(R.id.chipMarca)
        chipCategoria = findViewById(R.id.chipCategoria)
        chipEspecie = findViewById(R.id.chipEspecie)
        chipEtapa = findViewById(R.id.chipEtapa)

        switchGranel = findViewById(R.id.switchGranel)
        layoutGranel = findViewById(R.id.layoutGranel)
        etPrecioGranel = findViewById(R.id.etPrecioGranel)
        btnGuardar = findViewById(R.id.btnGuardar)

        val unidades = listOf("Pieza", "Kg", "Bulto", "Litro", "Caja")
        spUnidad.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, unidades))
    }

    private fun configurarListenersEspeciales() {
        spCategoria.setOnItemClickListener { parent, _, position, _ ->
            val nombre = parent.getItemAtPosition(position).toString()
            idCategoriaSeleccionada = listaCategoriasObj.find { it.nombre == nombre }?.id
        }

        spMarca.setOnItemClickListener { parent, _, position, _ ->
            val nombre = parent.getItemAtPosition(position).toString()
            idMarcaSeleccionada = listaMarcasObj.find { it.nombre == nombre }?.id
        }

        spEspecie.setOnItemClickListener { parent, _, position, _ ->
            val nombre = parent.getItemAtPosition(position).toString()
            idEspecieSeleccionada = listaEspeciesObj.find { it.nombre == nombre }?.id
        }

        spEtapa.setOnItemClickListener { parent, _, position, _ ->
            val nombre = parent.getItemAtPosition(position).toString()
            idEtapaSeleccionada = listaEtapasObj.find { it.nombre == nombre }?.id
        }
    }

    private fun cargarDatosDesdeAPI() {
        lifecycleScope.launch {
            try {
                // CATEGORÍAS
                val cats = RetrofitClient.instance.getCategorias()
                listaCategoriasObj.clear()
                listaCategoriasObj.addAll(cats)
                listaCategorias.clear()
                listaCategorias.addAll(cats.map { it.nombre })
                actualizarAdapter(spCategoria, listaCategorias)

                // TIPOS DE PRODUCTO (CORRECCIÓN AQUÍ)
                val tiposServer = RetrofitClient.instance.getTiposProducto()
                listaTiposObj.clear()
                listaTiposObj.addAll(tiposServer) // Objetos con ID
                listaTipos.clear()
                listaTipos.addAll(tiposServer.map { it.nombre }) // Solo los nombres (Strings)

                // Limpiamos los adaptadores duplicados que tenías
                actualizarAdapter(spTipo, listaTipos)

                // MARCAS
                val marcas = RetrofitClient.instance.getMarcas()
                listaMarcasObj.clear()
                listaMarcasObj.addAll(marcas)
                listaMarcas.clear()
                listaMarcas.addAll(marcas.map { it.nombre })
                actualizarAdapter(spMarca, listaMarcas)

                // CARGAR ESPECIES
                val especiesServer = RetrofitClient.instance.getEspecies()
                listaEspeciesObj.clear()
                listaEspeciesObj.addAll(especiesServer)
                listaEspecies.clear()
                listaEspecies.addAll(especiesServer.map { it.nombre })
                actualizarAdapter(spEspecie, listaEspecies)

                // CARGAR ETAPAS
                val etapasServer = RetrofitClient.instance.getEtapas()
                listaEtapasObj.clear()
                listaEtapasObj.addAll(etapasServer)
                listaEtapas.clear()
                listaEtapas.addAll(etapasServer.map { it.nombre })
                actualizarAdapter(spEtapa, listaEtapas)

            } catch (e: Exception) {
                Log.e("API_ERROR", "Error al cargar catálogos: ${e.message}")
            }
        }
    }


    private fun configurarChips() {
        chipMarca.setOnCheckedChangeListener { _, isChecked -> rowMarca.visibility = if (isChecked) View.VISIBLE else View.GONE }
        chipCategoria.setOnCheckedChangeListener { _, isChecked -> rowCategoria.visibility = if (isChecked) View.VISIBLE else View.GONE }
        chipEspecie.setOnCheckedChangeListener { _, isChecked -> rowEspecie.visibility = if (isChecked) View.VISIBLE else View.GONE }
        chipEtapa.setOnCheckedChangeListener { _, isChecked -> rowEtapa.visibility = if (isChecked) View.VISIBLE else View.GONE }
    }

    private fun configurarDropdownConEdicion(
        dropdown: AutoCompleteTextView,
        botonEditar: ImageButton,
        listaDatos: MutableList<String>,
        titulo: String
    ) {
        actualizarAdapter(dropdown, listaDatos)
        botonEditar.setOnClickListener {
            mostrarDialogoGestion(titulo, listaDatos) {
                actualizarAdapter(dropdown, listaDatos)
            }
        }
    }

    private fun actualizarAdapter(dropdown: AutoCompleteTextView, lista: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, lista)
        dropdown.setAdapter(adapter)
    }

    private fun mostrarDialogoGestion(titulo: String, lista: MutableList<String>, onUpdate: () -> Unit) {
        val opciones = arrayOf("➕ Agregar Nuevo", "🗑️ Borrar Existentes")
        MaterialAlertDialogBuilder(this)
            .setTitle("Gestionar $titulo")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarDialogoAgregar(titulo, lista, onUpdate)
                    1 -> mostrarDialogoBorrar(titulo, lista, onUpdate)
                }
            }
            .show()
    }

    private fun mostrarDialogoAgregar(titulo: String, lista: MutableList<String>, onUpdate: () -> Unit) {
        val input = EditText(this)
        input.hint = "Nombre de nuevo $titulo..."
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(50, 0, 50, 0)
        input.layoutParams = params
        container.addView(input)

        MaterialAlertDialogBuilder(this)
            .setTitle("Agregar a $titulo")
            .setView(container)
            .setPositiveButton("Guardar en Servidor") { _, _ ->
                val nombre = input.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    // Ejecutamos la llamada al servidor
                    lifecycleScope.launch {
                        try {
                            when (titulo) {
                                "Marcas" -> {
                                    val nueva = RetrofitClient.instance.crearMarca(MarcaIn(nombre))
                                    listaMarcasObj.add(nueva)
                                    lista.add(nueva.nombre)
                                }
                                "Categorías" -> {
                                    val nueva = RetrofitClient.instance.crearCategoria(CategoriaIn(nombre))
                                    listaCategoriasObj.add(nueva)
                                    lista.add(nueva.nombre)
                                }
                                "Especies" -> {
                                    val nueva = RetrofitClient.instance.crearEspecie(EspecieIn(nombre))
                                    listaEspeciesObj.add(nueva)
                                    lista.add(nueva.nombre)
                                }
                                "Etapas" -> {
                                    val nueva = RetrofitClient.instance.crearEtapa(EtapaIn(nombre))
                                    listaEtapasObj.add(nueva)
                                    lista.add(nueva.nombre)
                                }
                                // Busca este bloque dentro de mostrarDialogoAgregar
                                "Tipos de Producto" -> {
                                    // Usamos 'nombre' que ya definiste arriba con input.text.toString()
                                    if (nombre.isNotEmpty()) {
                                        val nueva = RetrofitClient.instance.crearTipoProducto(TipoProductoIn(nombre))
                                        listaTiposObj.add(nueva)
                                        lista.add(nueva.nombre)
                                    }
                                }
                            }
                            onUpdate() // Actualiza el dropdown visual
                            Toast.makeText(this@CrearProductoActivity, "✅ $nombre guardado", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("API_ERROR", "Error al crear atributo: ${e.message}")
                            Toast.makeText(this@CrearProductoActivity, "❌ Error al conectar con servidor", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun mostrarDialogoBorrar(titulo: String, lista: MutableList<String>, onUpdate: () -> Unit) {
        val itemsArray = lista.toTypedArray()
        val checkedItems = BooleanArray(lista.size)

        MaterialAlertDialogBuilder(this)
            .setTitle("Selecciona para borrar de $titulo")
            .setMultiChoiceItems(itemsArray, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Borrar del Servidor") { _, _ ->
                lifecycleScope.launch {
                    try {
                        for (i in checkedItems.indices.reversed()) {
                            if (checkedItems[i]) {
                                val nombreParaBorrar = lista[i]

                                when (titulo) {
                                    "Marcas" -> {
                                        val match = listaMarcasObj.find { it.nombre == nombreParaBorrar }
                                        match?.let { RetrofitClient.instance.eliminarMarca(it.id) }
                                        listaMarcasObj.removeAll { it.nombre == nombreParaBorrar }
                                    }
                                    "Categorías" -> {
                                        val match = listaCategoriasObj.find { it.nombre == nombreParaBorrar }
                                        match?.let { RetrofitClient.instance.eliminarCategoria(it.id) }
                                        listaCategoriasObj.removeAll { it.nombre == nombreParaBorrar }
                                    }
                                    "Tipos de Producto" -> {
                                        // Cambiamos 'it' por una variable explícita para evitar ambigüedad
                                        val match = listaTiposObj.find { tipo -> tipo.nombre == nombreParaBorrar }
                                        match?.let { tipo -> RetrofitClient.instance.eliminarTipoProducto(tipo.id) }
                                        listaTiposObj.removeAll { tipo -> tipo.nombre == nombreParaBorrar }
                                    }
                                    "Especies" -> {
                                        val match = listaEspeciesObj.find { it.nombre == nombreParaBorrar }
                                        match?.let { RetrofitClient.instance.eliminarEspecie(it.id) }
                                        listaEspeciesObj.removeAll { it.nombre == nombreParaBorrar }
                                    }
                                }
                                lista.removeAt(i)
                            }
                        }
                        onUpdate()
                        Toast.makeText(this@CrearProductoActivity, "🗑️ Eliminado permanentemente", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Error: ${e.message}")
                        Toast.makeText(this@CrearProductoActivity, "❌ Error al borrar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun configurarGranel() {
        switchGranel.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                layoutGranel.visibility = View.VISIBLE
            } else {
                layoutGranel.visibility = View.GONE
                etPrecioGranel.text?.clear()
            }
        }
    }

    private fun guardarProducto() {
        val nombre = etNombre.text.toString().trim()
        val precioTexto = etPrecioBase.text.toString().trim()
        val contenidoTexto = etContenido.text.toString().trim()

        // 1. Validaciones básicas
        if (nombre.isEmpty() || precioTexto.isEmpty() || contenidoTexto.isEmpty()) {
            Toast.makeText(this, "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Búsqueda manual de IDs (por si el click listener no se activó)
        if (idMarcaSeleccionada == null && spMarca.text.isNotEmpty()) {
            idMarcaSeleccionada = listaMarcasObj.find { it.nombre == spMarca.text.toString() }?.id
        }
        if (idCategoriaSeleccionada == null && spCategoria.text.isNotEmpty()) {
            idCategoriaSeleccionada = listaCategoriasObj.find { it.nombre == spCategoria.text.toString() }?.id
        }
        if (idEspecieSeleccionada == null && spEspecie.text.isNotEmpty()) {
            idEspecieSeleccionada = listaEspeciesObj.find { it.nombre == spEspecie.text.toString() }?.id
        }
        if (idEtapaSeleccionada == null && spEtapa.text.isNotEmpty()) {
            idEtapaSeleccionada = listaEtapasObj.find { it.nombre == spEtapa.text.toString() }?.id
        }

        // 3. Preparar valores numéricos y por defecto
        val precioBaseVal = precioTexto.toDoubleOrNull() ?: 0.0
        val contenidoVal = contenidoTexto.toDoubleOrNull() ?: 1.0
        val precioGranelVal = etPrecioGranel.text.toString().toDoubleOrNull()
        val stockMinVal = etStockMinimo.text.toString().toDoubleOrNull() ?: 5.0
        val tipoFinal = if (spTipo.text.toString().isEmpty()) "Alimento" else spTipo.text.toString()

        // 4. Crear el Request (Aquí es donde daban los errores)
        // Asegúrate de usar los nombres exactos: unidadMedida, precioBase, etc.
        val request = CrearProductoRequest(
            nombre = nombre,
            tipoProducto = tipoFinal,
            unidadMedida = spUnidad.text.toString(),
            precioBase = precioBaseVal,
            precioGranel = precioGranelVal,
            contenidoNeto = contenidoVal,
            seVendeAGranel = switchGranel.isChecked,
            marcaId = if (chipMarca.isChecked) idMarcaSeleccionada else null,
            categoriaId = if (chipCategoria.isChecked) idCategoriaSeleccionada else null,
            especieId = if (chipEspecie.isChecked) idEspecieSeleccionada else null,
            etapaId = if (chipEtapa.isChecked) idEtapaSeleccionada else null,
            stockMinimo = stockMinVal
        )

        // 5. Envío al servidor
        lifecycleScope.launch {
            try {
                Log.d("API_DEBUG", "Enviando JSON: ${com.google.gson.Gson().toJson(request)}")
                val response = RetrofitClient.instance.crearProducto(request)
                Toast.makeText(applicationContext, "✅ Producto Guardado", Toast.LENGTH_LONG).show()
                finish()
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error: ${e.message}")
                Toast.makeText(applicationContext, "❌ Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }

}