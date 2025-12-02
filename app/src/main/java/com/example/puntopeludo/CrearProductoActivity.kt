package com.example.puntopeludo

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CrearProductoActivity : AppCompatActivity() {

    // Variables para guardar la selección (pueden ser nulas)
    private var marcaSelect: Marca? = null
    private var catSelect: Categoria? = null
    private var espSelect: Especie? = null
    private var etapaSelect: Etapa? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_producto)

        // 1. Descargar datos del servidor
        cargarCatalogos()

        // 2. Configurar comportamiento visual
        configurarLogicaVisual()

        // 3. Configurar botones "+"
        configurarBotonesAgregar()

        // 4. Botón Guardar
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

                val unidadesDeferred = async { api.getUnidadesMedida() }

                // Llenar Spinners (Objetos)
                llenarSpinnerOpcional(findViewById(R.id.spMarca), marcasDeferred.await()) { marcaSelect = it }
                llenarSpinnerOpcional(findViewById(R.id.spCategoria), catsDeferred.await()) { catSelect = it }

                // Llenar Spinners (Texto simple)
                llenarSpinnerSimple(findViewById(R.id.spTipo), tiposDeferred.await())
                llenarSpinnerSimple(findViewById(R.id.spUnidad), unidadesDeferred.await())

                // Opcionales
                llenarSpinnerOpcional(findViewById(R.id.spEspecie), especiesDeferred.await()) { espSelect = it }
                llenarSpinnerOpcional(findViewById(R.id.spEtapa), etapasDeferred.await()) { etapaSelect = it }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CrearProductoActivity, "Error cargando datos", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- HELPER CORREGIDO: <T : Any> ---
    // El cambio clave está aquí: <T : Any> asegura que no sean nulos
    private fun <T : Any> llenarSpinnerOpcional(spinner: Spinner, listaOriginal: List<T>, onSelect: (T?) -> Unit) {
        val listaVisual = ArrayList<Any>()
        listaVisual.add("--- No Aplica / General ---")
        listaVisual.addAll(listaOriginal) // Ahora sí funciona

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaVisual)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if (pos == 0) {
                    onSelect(null)
                } else {
                    @Suppress("UNCHECKED_CAST")
                    onSelect(listaVisual[pos] as T)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun llenarSpinnerSimple(spinner: Spinner, lista: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lista)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    // --- 2. LÓGICA VISUAL ---
    private fun configurarLogicaVisual() {
        val spTipo = findViewById<Spinner>(R.id.spTipo)
        val layoutAlimento = findViewById<LinearLayout>(R.id.layoutDetallesAlimento)
        val cbGranel = findViewById<CheckBox>(R.id.cbGranel)
        val etPrecioGranel = findViewById<EditText>(R.id.etPrecioGranel)

        // Mostrar/Ocultar campos según tipo
        spTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                val tipo = spTipo.selectedItem?.toString() ?: ""

                // Lógica de negocio visual
                if (tipo == "Alimento") {
                    layoutAlimento.visibility = View.VISIBLE
                } else if (tipo == "Materia Prima") {
                    // Materia prima a veces usa categoría pero no especie
                    layoutAlimento.visibility = View.GONE
                } else {
                    layoutAlimento.visibility = View.GONE
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Mostrar/Ocultar precio granel
        cbGranel.setOnCheckedChangeListener { _, isChecked ->
            etPrecioGranel.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    // --- 3. BOTONES "+" ---
    private fun configurarBotonesAgregar() {
        val api = RetrofitClient.instance

        // Función local para recargar (También corregida con T : Any)
        fun <T : Any> recargarSpinner(
            spinner: Spinner,
            callApi: suspend () -> List<T>,
            onUpdate: (T?) -> Unit,
            nuevoNombre: String
        ) {
            lifecycleScope.launch {
                val nuevaLista = callApi()
                llenarSpinnerOpcional(spinner, nuevaLista, onUpdate)

                // Seleccionar el nuevo
                val adapter = spinner.adapter
                for (i in 0 until adapter.count) {
                    if (adapter.getItem(i).toString() == nuevoNombre) {
                        spinner.setSelection(i)
                        break
                    }
                }
            }
        }

        findViewById<ImageButton>(R.id.btnAddMarca).setOnClickListener {
            mostrarDialogoCrear("Nueva Marca") { nombre ->
                api.crearMarca(MarcaIn(nombre))
                recargarSpinner(findViewById(R.id.spMarca), { api.getMarcas() }, { marcaSelect = it }, nombre)
            }
        }

        findViewById<ImageButton>(R.id.btnAddCategoria).setOnClickListener {
            mostrarDialogoCrear("Nueva Categoría") { nombre ->
                api.crearCategoria(CategoriaIn(nombre))
                recargarSpinner(findViewById(R.id.spCategoria), { api.getCategorias() }, { catSelect = it }, nombre)
            }
        }

        findViewById<ImageButton>(R.id.btnAddEspecie).setOnClickListener {
            mostrarDialogoCrear("Nueva Especie") { nombre ->
                api.crearEspecie(EspecieIn(nombre))
                recargarSpinner(findViewById(R.id.spEspecie), { api.getEspecies() }, { espSelect = it }, nombre)
            }
        }

        findViewById<ImageButton>(R.id.btnAddEtapa).setOnClickListener {
            mostrarDialogoCrear("Nueva Etapa") { nombre ->
                api.crearEtapa(EtapaIn(nombre))
                recargarSpinner(findViewById(R.id.spEtapa), { api.getEtapas() }, { etapaSelect = it }, nombre)
            }
        }

        // Línea (ya no la usamos en el modelo simplificado, pero si la tienes en el XML, puedes dejarla o quitarla)
        // Si la borraste del XML, borra este bloque.
    }

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

    // --- 4. GUARDAR PRODUCTO ---
    private fun configurarBotonGuardar() {
        val btn = findViewById<Button>(R.id.btnGuardar)

        btn.setOnClickListener {
            try {
                val nombre = findViewById<EditText>(R.id.etNombre).text.toString()

                if (nombre.isEmpty()) {
                    Toast.makeText(this, "Falta Nombre", Toast.LENGTH_SHORT).show(); return@setOnClickListener
                }

                val tipo = findViewById<Spinner>(R.id.spTipo).selectedItem?.toString() ?: "Alimento"
                val unidad = findViewById<Spinner>(R.id.spUnidad).selectedItem?.toString() ?: "pza"

                val precioBaseStr = findViewById<EditText>(R.id.etPrecioBase).text.toString()
                val precioBase = if (precioBaseStr.isNotEmpty()) precioBaseStr.toDouble() else 0.0

                val contenidoStr = findViewById<EditText>(R.id.etContenido).text.toString()
                val contenido = if (contenidoStr.isNotEmpty()) contenidoStr.toDouble() else 1.0

                val esGranel = findViewById<CheckBox>(R.id.cbGranel).isChecked
                val stockMinStr = findViewById<EditText>(R.id.etStockMinimo).text.toString()
                val stockMin = if (stockMinStr.isNotEmpty()) stockMinStr.toDouble() else 5.0

                var precioGranel: Double? = null
                if (esGranel) {
                    val txtGranel = findViewById<EditText>(R.id.etPrecioGranel).text.toString()
                    if (txtGranel.isNotEmpty()) precioGranel = txtGranel.toDouble()
                }

                val nuevoProducto = CrearProductoRequest(
                    nombre = nombre,
                    tipoProducto = tipo,
                    unidadMedida = unidad,
                    precioBase = precioBase,
                    contenidoNeto = contenido,
                    seVendeAGranel = esGranel,
                    precioGranel = precioGranel,

                    marcaId = marcaSelect?.id,
                    categoriaId = catSelect?.id,

                    // Si no están visibles o se seleccionó "---", son null
                    especieId = if(tipo == "Alimento") espSelect?.id else null,
                    etapaId = if(tipo == "Alimento") etapaSelect?.id else null,

                )

                lifecycleScope.launch {
                    try {
                        RetrofitClient.instance.crearProducto(nuevoProducto)
                        Toast.makeText(this@CrearProductoActivity, "¡Producto Guardado!", Toast.LENGTH_LONG).show()
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@CrearProductoActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Error en datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}