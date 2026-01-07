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
    private var listaTipos = mutableListOf("Alimento", "Accesorio", "Farmacia", "Higiene")
    private var listaMarcas = mutableListOf("No Aplica", "Nupec", "ProPlan", "Bayer", "Whiskas", "Genérico")
    private var listaCategorias = mutableListOf("No Aplica", "Croquetas", "Sobres", "Collares", "Champú")
    private var listaEspecies = mutableListOf("No Aplica", "Perro", "Gato", "Ave", "Ganado")
    private var listaEtapas = mutableListOf("No Aplica", "Cachorro", "Adulto", "Senior", "Todas")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_producto)

        inicializarVistas()
        configurarChips()
        configurarGranel()

        // Configurar todas las listas con poder de edición
        configurarDropdownConEdicion(spTipo, btnEditTipo, listaTipos, "Tipos de Producto")
        configurarDropdownConEdicion(spMarca, btnEditMarca, listaMarcas, "Marcas")
        configurarDropdownConEdicion(spCategoria, btnEditCategoria, listaCategorias, "Categorías")
        configurarDropdownConEdicion(spEspecie, btnEditEspecie, listaEspecies, "Especies")
        configurarDropdownConEdicion(spEtapa, btnEditEtapa, listaEtapas, "Etapas")

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

        // Usamos los LinearLayout "row..." para ocultar todo el renglón (incluyendo el botón de editar)
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

        // Cargar lista de unidades (esa no necesita edición por ahora)
        val unidades = listOf("Pieza", "Kg", "Bulto", "Litro", "Caja")
        spUnidad.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, unidades))
    }

    private fun configurarChips() {
        chipMarca.setOnCheckedChangeListener { _, isChecked -> rowMarca.visibility = if (isChecked) View.VISIBLE else View.GONE }
        chipCategoria.setOnCheckedChangeListener { _, isChecked -> rowCategoria.visibility = if (isChecked) View.VISIBLE else View.GONE }
        chipEspecie.setOnCheckedChangeListener { _, isChecked -> rowEspecie.visibility = if (isChecked) View.VISIBLE else View.GONE }
        chipEtapa.setOnCheckedChangeListener { _, isChecked -> rowEtapa.visibility = if (isChecked) View.VISIBLE else View.GONE }
    }

    // --- FUNCIÓN MAESTRA PARA GESTIONAR LISTAS ---
    private fun configurarDropdownConEdicion(
        dropdown: AutoCompleteTextView,
        botonEditar: ImageButton,
        listaDatos: MutableList<String>,
        titulo: String
    ) {
        // 1. Cargar datos iniciales
        actualizarAdapter(dropdown, listaDatos)

        // 2. Configurar botón de edición
        botonEditar.setOnClickListener {
            mostrarDialogoGestion(titulo, listaDatos) {
                // Al cerrar el diálogo, actualizamos la lista visual
                actualizarAdapter(dropdown, listaDatos)
            }
        }
    }

    private fun actualizarAdapter(dropdown: AutoCompleteTextView, lista: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, lista)
        dropdown.setAdapter(adapter)
    }

    private fun mostrarDialogoGestion(titulo: String, lista: MutableList<String>, onUpdate: () -> Unit) {
        // Opciones del menú principal
        val opciones = arrayOf("➕ Agregar Nuevo", "🗑️ Borrar Existentes")

        MaterialAlertDialogBuilder(this)
            .setTitle("Gestionar $titulo")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarDialogoAgregar(titulo, lista, onUpdate) // Opción Agregar
                    1 -> mostrarDialogoBorrar(titulo, lista, onUpdate)  // Opción Borrar
                }
            }
            .show()
    }

    private fun mostrarDialogoAgregar(titulo: String, lista: MutableList<String>, onUpdate: () -> Unit) {
        val input = EditText(this)
        input.hint = "Escribe el nombre..."

        // Contenedor para darle margen al input
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(50, 0, 50, 0)
        input.layoutParams = params
        container.addView(input)

        MaterialAlertDialogBuilder(this)
            .setTitle("Agregar a $titulo")
            .setView(container)
            .setPositiveButton("Agregar") { _, _ ->
                val nuevoItem = input.text.toString().trim()
                if (nuevoItem.isNotEmpty()) {
                    lista.add(nuevoItem)
                    onUpdate()
                    Toast.makeText(this, "Agregado: $nuevoItem", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoBorrar(titulo: String, lista: MutableList<String>, onUpdate: () -> Unit) {
        // Convertimos la lista a Array para el diálogo
        val itemsArray = lista.toTypedArray()
        val checkedItems = BooleanArray(lista.size)

        MaterialAlertDialogBuilder(this)
            .setTitle("Selecciona para borrar")
            .setMultiChoiceItems(itemsArray, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Borrar Seleccionados") { _, _ ->
                // Borramos de atrás para adelante para no alterar los índices
                for (i in checkedItems.indices.reversed()) {
                    if (checkedItems[i]) {
                        lista.removeAt(i)
                    }
                }
                onUpdate()
                Toast.makeText(this, "Elementos borrados", Toast.LENGTH_SHORT).show()
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
        val nombre = etNombre.text.toString()
        val precioTexto = etPrecioBase.text.toString()
        val contenidoTexto = etContenido.text.toString()

        // 1. Validaciones
        if (nombre.isEmpty() || precioTexto.isEmpty() || contenidoTexto.isEmpty()) {
            Toast.makeText(this, "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Convertir datos
        val precioBase = precioTexto.toDouble()
        val contenido = contenidoTexto.toDouble()
        val esGranel = switchGranel.isChecked

        val precioGranel = if (esGranel && !etPrecioGranel.text.isNullOrEmpty()) {
            etPrecioGranel.text.toString().toDouble()
        } else {
            null
        }

        val tipoTxt = spTipo.text.toString()
        val unidadTxt = spUnidad.text.toString()

        // 3. Crear el objeto (AHORA CON LOS NOMBRES CORREGIDOS EN KOTLIN)
        val request = CrearProductoRequest(
            nombre = nombre,
            tipoProducto = tipoTxt,        // Antes era tipo_producto
            unidadMedida = unidadTxt,      // Antes era unidad_medida
            precioBase = precioBase,       // Antes era precio_base
            precioGranel = precioGranel,   // Antes era precio_granel
            contenidoNeto = contenido,     // Antes era contenido_neto
            seVendeAGranel = esGranel,     // Antes era se_vende_a_granel
            marcaId = null,                // Antes era marca_id
            categoriaId = null,
            especieId = null,
            etapaId = null
        )

        // 4. Enviar
        Toast.makeText(this, "Enviando...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                Log.d("API_DEBUG", "Enviando: $request")
                val response = RetrofitClient.instance.crearProducto(request)
                Log.d("API_DEBUG", "Éxito: $response")
                Toast.makeText(applicationContext, "✅ Producto Guardado", Toast.LENGTH_LONG).show()
                finish()
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error al guardar", e)
                Toast.makeText(applicationContext, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}