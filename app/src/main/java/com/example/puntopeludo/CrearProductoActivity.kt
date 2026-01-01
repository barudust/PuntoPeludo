package com.example.puntopeludo

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CrearProductoActivity : AppCompatActivity() {

    // Listas para almacenar los datos de los catálogos
    private var listaMarcas: List<Marca> = emptyList()
    private var listaCategorias: List<Categoria> = emptyList()
    private var listaEspecies: List<Especie> = emptyList()
    private var listaEtapas: List<Etapa> = emptyList()

    // Variables para guardar la selección
    private var marcaSelect: Marca? = null
    private var catSelect: Categoria? = null
    private var espSelect: Especie? = null
    private var etapaSelect: Etapa? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_producto)

        // 1. Configurar botón de regreso
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish() // Cierra la actividad actual
        }

        // 2. Descargar datos del servidor
        cargarCatalogos()

        // 3. Configurar comportamiento visual
        configurarLogicaVisual()

        // 4. Configurar botones "+"
        configurarBotonesAgregar()

        // 5. Botón Guardar
        configurarBotonGuardar()
    }

    // --- 1. CARGA DE DATOS ---
    private fun cargarCatalogos() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instance

                // Descargas en paralelo
                val marcasDeferred = async { api.getMarcas() }
                val catsDeferred = async { api.getCategorias() }
                val tiposDeferred = async { api.getTiposProducto() }
                val especiesDeferred = async { api.getEspecies() }
                val etapasDeferred = async { api.getEtapas() }

                // Asignar listas
                listaMarcas = marcasDeferred.await()
                listaCategorias = catsDeferred.await()
                listaEspecies = especiesDeferred.await()
                listaEtapas = etapasDeferred.await()

                // Llenar Dropdowns (versión moderna de Spinners)
                configurarDropdown(findViewById(R.id.actvTipo), tiposDeferred.await())
                configurarDropdown(findViewById(R.id.actvMarca), listaMarcas.map { it.nombre }) { pos ->
                    marcaSelect = listaMarcas.getOrNull(pos)
                }
                // Aquí configurarías los demás dropdowns (actvCategoria, etc.) de manera similar

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CrearProductoActivity, "Error cargando datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- HELPER para AutoCompleteTextView (el nuevo Spinner) ---
    private fun <T> configurarDropdown(autoCompleteTextView: AutoCompleteTextView, items: List<T>, onSelect: ((Int) -> Unit)? = null) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        autoCompleteTextView.setAdapter(adapter)
        if (onSelect != null) {
            autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                onSelect(position)
            }
        }
    }


    // --- 2. LÓGICA VISUAL ---
    private fun configurarLogicaVisual() {
        val actvTipo = findViewById<AutoCompleteTextView>(R.id.actvTipo)
        val layoutAtributos = findViewById<View>(R.id.layoutAtributos) // Ahora es un LinearLayout dentro de una Card
        val switchGranel = findViewById<SwitchMaterial>(R.id.switchGranel)
        val layoutPrecioGranel = findViewById<TextInputLayout>(R.id.layoutPrecioGranel)

        // Mostrar/Ocultar campos según tipo
        actvTipo.setOnItemClickListener { _, _, position, _ ->
            val tipo = actvTipo.adapter.getItem(position)?.toString() ?: ""

            // En el nuevo diseño, no ocultamos el layout completo de atributos,
            // sino los campos específicos de "Alimento" que están dentro.
            // val layoutDetallesAlimento = findViewById<LinearLayout>(R.id.layoutDetallesAlimento)
            // layoutDetallesAlimento.visibility = if (tipo == "Alimento") View.VISIBLE else View.GONE
        }

        // Mostrar/Ocultar precio granel
        switchGranel.setOnCheckedChangeListener { _, isChecked ->
            layoutPrecioGranel.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    // --- 3. BOTONES "+" ---
    private fun configurarBotonesAgregar() {
        findViewById<ImageButton>(R.id.btnAddMarca).setOnClickListener {
            // La lógica de mostrarDialogoCrear y recargar sigue siendo válida
            // pero necesitarás adaptarla para que funcione con los nuevos AutoCompleteTextView
            Toast.makeText(this, "Funcionalidad 'Agregar Marca' pendiente de adaptar.", Toast.LENGTH_SHORT).show()
        }
        // Repetir para btnAddCategoria, btnAddEspecie, etc.
    }

    // --- 4. GUARDAR PRODUCTO ---
    private fun configurarBotonGuardar() {
        findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            try {
                val nombre = findViewById<TextInputEditText>(R.id.etNombre).text.toString()

                if (nombre.isEmpty()) {
                    Toast.makeText(this, "El nombre del producto es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val tipo = findViewById<AutoCompleteTextView>(R.id.actvTipo).text.toString()
                val precioBaseStr = findViewById<TextInputEditText>(R.id.etPrecioBase).text.toString()
                val stockMinStr = findViewById<TextInputEditText>(R.id.etStockMinimo).text.toString()
                val esGranel = findViewById<SwitchMaterial>(R.id.switchGranel).isChecked
                val precioGranelStr = findViewById<TextInputEditText>(R.id.etPrecioGranel).text.toString()

                // Validaciones y conversiones
                val precioBase = precioBaseStr.toDoubleOrNull() ?: 0.0
                val stockMin = stockMinStr.toDoubleOrNull() ?: 5.0
                val precioGranel = if (esGranel) precioGranelStr.toDoubleOrNull() else null

                val nuevoProducto = CrearProductoRequest(
                    nombre = nombre,
                    tipoProducto = tipo,
                    unidadMedida = "pza", // Ajustar si tienes un dropdown para esto
                    precioBase = precioBase,
                    contenidoNeto = 1.0, // Ajustar si tienes un campo para esto
                    seVendeAGranel = esGranel,
                    precioGranel = precioGranel,
                    marcaId = marcaSelect?.id,
                    categoriaId = catSelect?.id,
                    especieId = if (tipo == "Alimento") espSelect?.id else null,
                    etapaId = if (tipo == "Alimento") etapaSelect?.id else null
                )

                lifecycleScope.launch {
                    try {
                        RetrofitClient.instance.crearProducto(nuevoProducto)
                        Toast.makeText(this@CrearProductoActivity, "¡Producto Guardado!", Toast.LENGTH_LONG).show()
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@CrearProductoActivity, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Error en los datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Las funciones `mostrarDialogoCrear` y otras que no dependen directamente de los IDs del layout
    // pueden permanecer como estaban.
    private fun mostrarDialogoCrear(titulo: String, onGuardar: suspend (String) -> Unit) {
        val input = EditText(this)
        input.hint = "Nombre"

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val texto = input.text.toString()
                if (texto.isNotEmpty()) {
                    lifecycleScope.launch {
                        try {
                            onGuardar(texto)
                            Toast.makeText(this@CrearProductoActivity, "Creado", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@CrearProductoActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
