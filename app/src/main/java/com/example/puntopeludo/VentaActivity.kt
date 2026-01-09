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
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class VentaActivity : AppCompatActivity() {

    private lateinit var adapterCarrito: VentaCarritoAdapter
    private lateinit var tvTotal: TextView
    private lateinit var autoCompleteProducto: AutoCompleteTextView
    private lateinit var autoCompleteCliente: AutoCompleteTextView
    private lateinit var chipGroup: ChipGroup

    // Adaptador personalizado que mantiene los objetos reales
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

        // 1. Vincular Vistas
        tvTotal = findViewById(R.id.textViewTotal)
        autoCompleteProducto = findViewById(R.id.autoCompleteProducto)
        autoCompleteCliente = findViewById(R.id.autoCompleteCliente)
        chipGroup = findViewById(R.id.chipGroupFiltros)
        val rvCarrito = findViewById<RecyclerView>(R.id.recyclerViewCarrito)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // 2. Configurar Carrito
        adapterCarrito = VentaCarritoAdapter(listaCarrito) { calcularTotal() }
        rvCarrito.layoutManager = LinearLayoutManager(this)
        rvCarrito.adapter = adapterCarrito

        // 3. Configurar Cliente
        autoCompleteCliente.inputType = 0
        autoCompleteCliente.setOnClickListener { autoCompleteCliente.showDropDown() }

        // 4. Configurar Buscador de Productos (SIN TextWatcher manual)
        productoAdapter = ProductoAdapter(this, mutableListOf())
        autoCompleteProducto.setAdapter(productoAdapter)
        autoCompleteProducto.threshold = 1

        // CLICK LISTENER: Aquí está la magia. Obtenemos el OBJETO directo.
        autoCompleteProducto.setOnItemClickListener { parent, _, position, _ ->
            try {
                val producto = parent.getItemAtPosition(position) as ProductoResponse
                agregarAlCarrito(producto)

                // Limpiamos el texto después de agregar
                autoCompleteProducto.setText("")
                autoCompleteProducto.clearFocus()
            } catch (e: Exception) {
                Log.e("VENTA", "Error al seleccionar: ${e.message}")
            }
        }

        // 5. Cargar Datos
        cargarDatosServidor()

        // 6. Filtros por Chip (Actualizan el adaptador)
        chipGroup.setOnCheckedStateChangeListener { _, _ ->
            // Forzamos al adaptador a refrescar su filtro con el nuevo chip
            productoAdapter.actualizarFiltroChip(obtenerFiltroDeChip())
            if (autoCompleteProducto.text.isNotEmpty()) {
                productoAdapter.filter.filter(autoCompleteProducto.text)
            }
        }

        findViewById<MaterialButton>(R.id.buttonFinalizarVenta).setOnClickListener { cobrarVenta() }
    }

    private fun cargarDatosServidor() {
        lifecycleScope.launch {
            try {
                // Cargar Marcas
                val marcas = RetrofitClient.instance.getMarcas()
                marcas.forEach { mapaMarcas[it.id] = it.nombre }

                // Cargar Clientes
                listaClientes = RetrofitClient.instance.getClientes().toMutableList()
                val adapterClie = ArrayAdapter(this@VentaActivity, android.R.layout.simple_dropdown_item_1line, listaClientes.map { it.nombre })
                autoCompleteCliente.setAdapter(adapterClie)
                autoCompleteCliente.setOnItemClickListener { parent, _, position, _ ->
                    val nombreSel = parent.getItemAtPosition(position) as String
                    clienteSeleccionadoId = listaClientes.find { it.nombre == nombreSel }?.id
                }

                // Cargar Productos e inicializar adaptador
                inventarioReal = RetrofitClient.instance.getProductos().toMutableList()
                reglasDescuento = RetrofitClient.instance.getDescuentos().toMutableList()

                // Le damos la lista completa al adaptador
                productoAdapter.setListaCompleta(inventarioReal)

            } catch (e: Exception) {
                Log.e("VENTA_ERROR", "Error carga: ${e.message}")
                Toast.makeText(this@VentaActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerFiltroDeChip(): String {
        val chipId = chipGroup.checkedChipId
        if (chipId == -1) return "todos"
        val chipText = findViewById<Chip>(chipId).text.toString().lowercase()
        return when {
            chipText.contains("perro") -> "perro"
            chipText.contains("gato") -> "gato"
            chipText.contains("granel") -> "granel"
            chipText.contains("farmacia") -> "farmacia"
            else -> "todos"
        }
    }

    private fun agregarAlCarrito(prod: ProductoResponse) {
        // 1. Determinar Precio Correcto (Paquete vs Granel)
        // Si el producto es a granel y tiene precio especial, usamos ese.
        var precioBaseCalc = prod.precioBase
        if (prod.esGranel && prod.precioGranel != null && prod.precioGranel > 0) {
            precioBaseCalc = prod.precioGranel
        }

        // 2. BUSCADOR DE REGLAS (Lógica "Waterfall" estricta)
        // Filtramos solo las reglas que cumplan TODAS las condiciones no nulas
        val mejorRegla = reglasDescuento.filter { regla ->
            // A. ¿La regla es para este producto? (O es para todos los productos)
            val matchProducto = regla.productoId == null || regla.productoId == prod.id

            // B. ¿La regla es para esta marca? (O es para todas las marcas)
            val matchMarca = regla.marcaId == null || regla.marcaId == prod.marcaId

            // C. ¿La regla es para este cliente? (O es para todos los clientes)
            val matchCliente = regla.clienteId == null || regla.clienteId == clienteSeleccionadoId

            // La regla solo es válida si CUMPLE CON TODO
            matchProducto && matchMarca && matchCliente
        }.sortedWith(compareByDescending<ReglaDescuento> { it.productoId != null } // Preferir regla de Producto
            .thenByDescending { it.marcaId != null }     // Luego regla de Marca
            .thenByDescending { it.clienteId != null }   // Luego regla de Cliente
            .thenByDescending { it.descuentoPorcentaje } // Finalmente, la que de más %
        ).firstOrNull()

        // 3. Aplicar descuento si existe
        val porcentaje = mejorRegla?.descuentoPorcentaje ?: 0.0
        val precioFinal = precioBaseCalc * (1 - (porcentaje / 100))

        // 4. Agregar al adaptador
        adapterCarrito.agregarProducto(ProductoCarrito(
            producto_id = prod.id,
            nombre = prod.nombre,
            precio_unitario = precioFinal,
            cantidad = 1.0,
            es_granel = prod.esGranel
        ))

        calcularTotal()

        // Feedback visual
        if (porcentaje > 0) {
            Toast.makeText(this, "Descuento aplicado: $porcentaje%", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calcularTotal() {
        val total = adapterCarrito.obtenerLista().sumOf { it.precio_unitario * it.cantidad }
        tvTotal.text = NumberFormat.getCurrencyInstance(Locale.US).format(total)
    }

    private fun cobrarVenta() {
        val prefs = getSharedPreferences("PuntoPeludoPrefs", MODE_PRIVATE)
        val sId = prefs.getInt("ID_SUCURSAL_SESION", 0)
        val uId = prefs.getInt("ID_USUARIO_SESION", 0)

        lifecycleScope.launch {
            try {
                val pedido = VentaCreateReq(
                    sucursal_id = sId,
                    usuario_id = uId,
                    cliente_id = clienteSeleccionadoId,
                    detalles = adapterCarrito.obtenerLista().map { DetalleVentaReq(it.producto_id, it.cantidad) }
                )
                RetrofitClient.instance.registrarVenta(pedido)
                Toast.makeText(this@VentaActivity, "✅ Venta Exitosa", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@VentaActivity, "❌ Error al cobrar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ===============================================================
    // ADAPTADOR INTERNO PODEROSO (Maneja filtrado y objetos reales)
    // ===============================================================
    inner class ProductoAdapter(context: Context, var items: MutableList<ProductoResponse>)
        : ArrayAdapter<ProductoResponse>(context, android.R.layout.simple_dropdown_item_1line, items) {

        private var listaOriginal = ArrayList<ProductoResponse>(items)
        private var filtroActualChip = "todos"

        fun setListaCompleta(lista: List<ProductoResponse>) {
            listaOriginal = ArrayList(lista)
            notifyDataSetChanged()
        }

        fun actualizarFiltroChip(nuevoFiltro: String) {
            filtroActualChip = nuevoFiltro
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // Personalizamos cómo se ve el texto en la lista desplegable
            val view = super.getView(position, convertView, parent)
            val item = getItem(position)
            if (view is TextView && item != null) {
                val marca = mapaMarcas[item.marcaId] ?: "Sin Marca"
                view.text = "${item.nombre} ($marca)"
            }
            return view
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val query = constraint?.toString()?.lowercase() ?: ""
                    val results = FilterResults()

                    // Filtramos sobre la lista original segura
                    val filtrados = listaOriginal.filter { prod ->
                        val coincideChip = when (filtroActualChip) {
                            "todos" -> true
                            "granel" -> prod.esGranel
                            "perro" -> prod.especieNombre?.lowercase()?.contains("perro") == true
                            "gato" -> prod.especieNombre?.lowercase()?.contains("gato") == true
                            "farmacia" -> prod.tipoProducto?.lowercase()?.contains("farmacia") == true
                            else -> true
                        }
                        val marca = (mapaMarcas[prod.marcaId] ?: "").lowercase()
                        val coincideTexto = prod.nombre.lowercase().contains(query) || marca.contains(query)

                        coincideChip && coincideTexto
                    }

                    results.values = filtrados
                    results.count = filtrados.size
                    return results
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    clear()
                    if (results != null && results.count > 0) {
                        addAll(results.values as List<ProductoResponse>)
                    }
                    notifyDataSetChanged()
                }

                // Evitamos que al seleccionar se convierta en String el objeto en el cuadro de texto
                override fun convertResultToString(resultValue: Any?): CharSequence {
                    return "" // Devolvemos vacío para que al seleccionar se limpie visualmente o no moleste
                }
            }
        }
    }
}