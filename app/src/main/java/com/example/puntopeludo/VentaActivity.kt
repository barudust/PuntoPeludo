package com.example.puntopeludo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class VentaActivity : AppCompatActivity() {

    private lateinit var adapterCarrito: VentaCarritoAdapter
    private lateinit var tvTotal: TextView
    private lateinit var autoCompleteProducto: AutoCompleteTextView
    private lateinit var autoCompleteCliente: AutoCompleteTextView
    private lateinit var chipGroup: ChipGroup

    private lateinit var productoAdapter: ProductoAdapter

    private var inventarioReal = mutableListOf<ProductoResponse>()
    private var listaClientes = mutableListOf<Cliente>()
    private var reglasDescuento = mutableListOf<ReglaDescuento>()
    private var clienteSeleccionadoId: Int? = null
    private var mapaMarcas = mutableMapOf<Int, String>()
    private var listaCarrito = mutableListOf<ProductoCarrito>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta)

        tvTotal = findViewById(R.id.textViewTotal)
        autoCompleteProducto = findViewById(R.id.autoCompleteProducto)
        autoCompleteCliente = findViewById(R.id.autoCompleteCliente)
        chipGroup = findViewById(R.id.chipGroupFiltros)
        val rvCarrito = findViewById<RecyclerView>(R.id.recyclerViewCarrito)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        adapterCarrito = VentaCarritoAdapter(listaCarrito) { calcularTotal() }
        rvCarrito.layoutManager = LinearLayoutManager(this)
        rvCarrito.adapter = adapterCarrito

        autoCompleteCliente.inputType = 0
        autoCompleteCliente.setText("Público General")
        autoCompleteCliente.setOnClickListener { autoCompleteCliente.showDropDown() }

        productoAdapter = ProductoAdapter(this, mutableListOf())
        autoCompleteProducto.setAdapter(productoAdapter)
        autoCompleteProducto.threshold = 1

        autoCompleteProducto.setOnItemClickListener { parent, _, position, _ ->
            val producto = parent.getItemAtPosition(position) as ProductoResponse
            procesarSeleccionProducto(producto)
            autoCompleteProducto.setText("")
            autoCompleteProducto.clearFocus()
        }

        cargarDatosServidor()

        // Listener dinámico para los chips
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chipId = if (checkedIds.isNotEmpty()) checkedIds[0] else -1
            val filtroTexto = if (chipId != -1) {
                group.findViewById<Chip>(chipId).text.toString()
            } else {
                "Todos"
            }

            productoAdapter.actualizarFiltroChip(filtroTexto)

            // Refrescar resultados incluso si el buscador está vacío
            if (autoCompleteProducto.text.isNotEmpty()) {
                productoAdapter.filter.filter(autoCompleteProducto.text)
            } else {
                // Truco: filtrar con texto vacío para mostrar sugerencias basadas solo en el chip
                productoAdapter.filter.filter("")
                if (filtroTexto != "Todos") {
                    autoCompleteProducto.showDropDown()
                }
            }
        }

        findViewById<MaterialButton>(R.id.buttonFinalizarVenta).setOnClickListener { cobrarVenta() }
    }

    private fun cargarDatosServidor() {
        lifecycleScope.launch {
            try {
                // 1. Cargar datos base
                val marcas = RetrofitClient.instance.getMarcas()
                mapaMarcas = marcas.associate { it.id to it.nombre }.toMutableMap()

                listaClientes = RetrofitClient.instance.getClientes().toMutableList()
                val nombresClientes = listaClientes.map { it.nombre }.toMutableList()
                nombresClientes.add(0, "Público General")

                val adapterClie = ArrayAdapter(this@VentaActivity, android.R.layout.simple_dropdown_item_1line, nombresClientes)
                autoCompleteCliente.setAdapter(adapterClie)

                autoCompleteCliente.setOnItemClickListener { parent, _, position, _ ->
                    val sel = parent.getItemAtPosition(position) as String
                    if (sel == "Público General") clienteSeleccionadoId = null
                    else clienteSeleccionadoId = listaClientes.find { it.nombre == sel }?.id
                    recalcularPreciosCarrito()
                }

                // 2. Cargar Productos y Generar Filtros
                inventarioReal = RetrofitClient.instance.obtenerProductos(mostrarInactivos = false).toMutableList()
                reglasDescuento = RetrofitClient.instance.getDescuentos().toMutableList()

                productoAdapter.setListaCompleta(inventarioReal)

                // --- AQUÍ ESTÁ LA MAGIA: FILTROS DINÁMICOS ---
                generarFiltrosInteligentes()

            } catch (e: Exception) {
                Toast.makeText(this@VentaActivity, "Error carga: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generarFiltrosInteligentes() {
        chipGroup.removeAllViews() // Limpiamos lo que haya en el XML

        // 1. Chip "Todos" (Obligatorio)
        agregarChip("Todos", true)

        // 2. Chip "Granel" (Solo si existen productos a granel)
        if (inventarioReal.any { it.esGranel }) {
            agregarChip("A Granel")
        }

        // 3. Chips de Categorías (Ej. Perro, Gato) - Extraídos de los productos reales
        val categoriasUnicas = inventarioReal.mapNotNull { it.categoriaNombre }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        categoriasUnicas.forEach { categoria ->
            agregarChip(categoria)
        }

        // 4. Chips de Tipos (Ej. Farmacia, Accesorios)
        val tiposUnicos = inventarioReal.mapNotNull { it.tipoProducto }
            .filter { it != "Alimento" && it.isNotEmpty() } // "Alimento" suele ser redundante si ya tenemos Perro/Gato
            .distinct()
            .sorted()

        tiposUnicos.forEach { tipo ->
            agregarChip(tipo)
        }
    }

    private fun agregarChip(texto: String, seleccionado: Boolean = false) {
        val chip = LayoutInflater.from(this).inflate(R.layout.item_chip_filtro, chipGroup, false) as Chip
        chip.text = texto
        chip.id = View.generateViewId()
        chip.isChecked = seleccionado
        chipGroup.addView(chip)
    }

    // --- Adaptador Interno Potenciado ---
    inner class ProductoAdapter(context: Context, var items: MutableList<ProductoResponse>)
        : ArrayAdapter<ProductoResponse>(context, android.R.layout.simple_dropdown_item_1line, items) {

        private var listaOriginal = ArrayList<ProductoResponse>(items)
        private var filtroChipActual = "Todos"

        fun setListaCompleta(lista: List<ProductoResponse>) {
            listaOriginal = ArrayList(lista)
            notifyDataSetChanged()
        }

        fun actualizarFiltroChip(nuevoFiltro: String) {
            filtroChipActual = nuevoFiltro
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val item = getItem(position)
            if (view is TextView && item != null) {
                // Mostramos Nombre + Marca para desambiguar
                val marca = mapaMarcas[item.marcaId] ?: ""
                val extra = if(marca.isNotEmpty()) "($marca)" else ""
                view.text = "${item.nombre} $extra"
            }
            return view
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val query = constraint?.toString()?.lowercase() ?: ""
                    val results = FilterResults()

                    val filtrados = listaOriginal.filter { prod ->
                        // LÓGICA DE FILTRO "INTELIGENTE"
                        // El chip actúa como un filtro maestro
                        val pasaChip = when (filtroChipActual) {
                            "Todos" -> true
                            "A Granel" -> prod.esGranel
                            else -> {
                                // Buscamos coincidencia en Categoría, Tipo o Marca
                                val cat = prod.categoriaNombre ?: ""
                                val tipo = prod.tipoProducto ?: ""
                                val marca = mapaMarcas[prod.marcaId] ?: ""

                                cat.equals(filtroChipActual, ignoreCase = true) ||
                                        tipo.equals(filtroChipActual, ignoreCase = true) ||
                                        marca.equals(filtroChipActual, ignoreCase = true)
                            }
                        }

                        // El texto filtra por nombre o marca
                        val marcaNombre = (mapaMarcas[prod.marcaId] ?: "").lowercase()
                        val pasaTexto = prod.nombre.lowercase().contains(query) || marcaNombre.contains(query)

                        pasaChip && pasaTexto
                    }

                    results.values = filtrados
                    results.count = filtrados.size
                    return results
                }

                @Suppress("UNCHECKED_CAST")
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    clear()
                    if (results != null && results.count > 0) {
                        addAll(results.values as List<ProductoResponse>)
                    }
                    notifyDataSetChanged()
                }

                override fun convertResultToString(resultValue: Any?): CharSequence = ""
            }
        }
    }

    // ... (El resto de funciones: agregarAlCarrito, procesarSeleccionProducto, cobrarVenta, recalcularPreciosCarrito siguen igual que en la versión anterior exitosa) ...
    // Solo asegúrate de copiar esas funciones aquí si copiaste el archivo entero.

    // --- COPIA ESTAS FUNCIONES DEL ARCHIVO ANTERIOR ---
    private fun recalcularPreciosCarrito() {
        val itemsActuales = adapterCarrito.obtenerLista()
        if (itemsActuales.isEmpty()) return
        for (i in itemsActuales.indices) {
            val item = itemsActuales[i]
            val prodOriginal = inventarioReal.find { it.id == item.producto_id }
            if (prodOriginal != null) {
                val nuevoPrecio = calcularPrecioUnitario(prodOriginal, item.es_granel)
                itemsActuales[i] = item.copy(precio_unitario = nuevoPrecio)
            }
        }
        adapterCarrito.notifyDataSetChanged()
        calcularTotal()
    }

    private fun calcularPrecioUnitario(prod: ProductoResponse, esGranel: Boolean): Double {
        val precioBaseCalc = if (esGranel) prod.precioGranel!! else prod.precioBase
        val mejorRegla = reglasDescuento.filter { regla ->
            val matchProd = regla.productoId == null || regla.productoId == prod.id
            val matchMarca = regla.marcaId == null || regla.marcaId == prod.marcaId
            val matchCliente = regla.clienteId == null || regla.clienteId == clienteSeleccionadoId
            regla.activo && matchProd && matchMarca && matchCliente
        }.maxByOrNull { it.descuentoPorcentaje }
        val porcentaje = mejorRegla?.descuentoPorcentaje ?: 0.0
        return precioBaseCalc * (1 - (porcentaje / 100))
    }

    private fun procesarSeleccionProducto(prod: ProductoResponse) {
        if (!prod.esGranel || prod.precioGranel == null || prod.precioGranel == 0.0) {
            agregarAlCarrito(prod, esVentaGranel = false)
            return
        }
        val opciones = arrayOf("📦 Paquete Cerrado ($${prod.precioBase})", "⚖️ A Granel / Kilos ($${prod.precioGranel})")
        MaterialAlertDialogBuilder(this).setTitle("¿Cómo vender ${prod.nombre}?")
            .setItems(opciones) { _, which -> agregarAlCarrito(prod, esVentaGranel = (which == 1)) }
            .show()
    }

    private fun agregarAlCarrito(prod: ProductoResponse, esVentaGranel: Boolean) {
        val precioFinal = calcularPrecioUnitario(prod, esVentaGranel)
        val itemExistente = adapterCarrito.obtenerLista().find { it.producto_id == prod.id && it.es_granel == esVentaGranel }
        if (itemExistente != null) {
            itemExistente.cantidad += 1.0
            adapterCarrito.notifyDataSetChanged()
            Toast.makeText(this, "+1 Agregado", Toast.LENGTH_SHORT).show()
        } else {
            adapterCarrito.agregarProducto(ProductoCarrito(
                producto_id = prod.id, nombre = prod.nombre, precio_unitario = precioFinal,
                cantidad = 1.0, es_granel = esVentaGranel, contenido_neto = prod.contenidoNeto ?: 1.0, precio_granel = prod.precioGranel
            ))
        }
        calcularTotal()
    }

    private fun calcularTotal() {
        val total = adapterCarrito.obtenerLista().sumOf { it.precio_unitario * it.cantidad }
        tvTotal.text = NumberFormat.getCurrencyInstance(Locale.US).format(total)
    }

    private fun cobrarVenta() {
        val prefs = getSharedPreferences("PuntoPeludoPrefs", MODE_PRIVATE)
        lifecycleScope.launch {
            try {
                val detallesReq = adapterCarrito.obtenerLista().map { DetalleVentaReq(it.producto_id, it.cantidad, it.es_granel) }
                val pedido = VentaCreateReq(
                    sucursal_id = prefs.getInt("ID_SUCURSAL_SESION", 0),
                    usuario_id = prefs.getInt("ID_USUARIO_SESION", 0),
                    cliente_id = clienteSeleccionadoId,
                    detalles = detallesReq
                )
                RetrofitClient.instance.registrarVenta(pedido)
                Toast.makeText(this@VentaActivity, "✅ Venta Exitosa", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@VentaActivity, "Error al cobrar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}