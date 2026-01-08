package com.example.puntopeludo

import android.os.Bundle
import android.util.Log
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

    private var inventarioReal = mutableListOf<ProductoResponse>()
    private var listaClientes = mutableListOf<Cliente>()
    private var reglasDescuento = mutableListOf<ReglaDescuento>()
    private var clienteSeleccionadoId: Int? = null
    private var mapaMarcas = mutableMapOf<Int, String>()
    private var listaCarrito = mutableListOf<ProductoCarrito>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta)

        // Vincular Vistas
        tvTotal = findViewById(R.id.textViewTotal)
        autoCompleteProducto = findViewById(R.id.autoCompleteProducto)
        autoCompleteCliente = findViewById(R.id.autoCompleteCliente)
        chipGroup = findViewById(R.id.chipGroupFiltros)
        val rvCarrito = findViewById<RecyclerView>(R.id.recyclerViewCarrito)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Configurar RecyclerView
        adapterCarrito = VentaCarritoAdapter(listaCarrito) { calcularTotal() }
        rvCarrito.layoutManager = LinearLayoutManager(this)
        rvCarrito.adapter = adapterCarrito

        cargarDatosServidor()
        configurarFiltros()

        findViewById<MaterialButton>(R.id.buttonFinalizarVenta).setOnClickListener { cobrarVenta() }
    }

    private fun cargarDatosServidor() {
        lifecycleScope.launch {
            try {
                // 1. Cargar Marcas
                val marcas = RetrofitClient.instance.getMarcas()
                marcas.forEach { mapaMarcas[it.id] = it.nombre }

                // 2. Cargar Clientes
                listaClientes = RetrofitClient.instance.getClientes().toMutableList()
                configurarBuscadorCliente()

                // 3. Cargar Productos y Descuentos
                inventarioReal = RetrofitClient.instance.getProductos().toMutableList()
                reglasDescuento = RetrofitClient.instance.getDescuentos().toMutableList()

                actualizarBuscador("todos")
            } catch (e: Exception) {
                Log.e("VENTA_ERROR", "Error de carga: ${e.message}")
            }
        }
    }

    private fun configurarBuscadorCliente() {
        val nombres = listaClientes.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombres)
        autoCompleteCliente.setAdapter(adapter)
        autoCompleteCliente.setOnItemClickListener { parent, _, position, _ ->
            val seleccion = parent.getItemAtPosition(position) as String
            clienteSeleccionadoId = listaClientes.find { it.nombre == seleccion }?.id
            Toast.makeText(this, "Cliente: $seleccion", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarFiltros() {
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val chipId = checkedIds.firstOrNull()
            if (chipId != null) {
                val chipText = findViewById<Chip>(chipId).text.toString().lowercase()
                // Mapeo inteligente de chips a atributos del schema
                when {
                    chipText.contains("perro") -> actualizarBuscador("especie:perro")
                    chipText.contains("gato") -> actualizarBuscador("especie:gato")
                    chipText.contains("granel") -> actualizarBuscador("granel")
                    chipText.contains("farmacia") -> actualizarBuscador("farmacia")
                    else -> actualizarBuscador("todos")
                }
            } else {
                actualizarBuscador("todos")
            }
        }
    }

    private fun actualizarBuscador(filtro: String) {
        val listaFiltrada = when (filtro) {
            "todos" -> inventarioReal
            "granel" -> inventarioReal.filter { it.esGranel } //
            "especie:perro" -> inventarioReal.filter { it.especieNombre?.lowercase()?.contains("perro") == true }
            "especie:gato" -> inventarioReal.filter { it.especieNombre?.lowercase()?.contains("gato") == true }
            "farmacia" -> inventarioReal.filter { it.tipoProducto?.lowercase()?.contains("farmacia") == true }
            else -> inventarioReal
        }

        val sugerencias = listaFiltrada.map { "${it.nombre} (${mapaMarcas[it.marcaId] ?: "Sin Marca"})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sugerencias)
        autoCompleteProducto.setAdapter(adapter)

        autoCompleteProducto.setOnItemClickListener { parent, _, position, _ ->
            val seleccion = parent.getItemAtPosition(position) as String
            val nombreLimpio = seleccion.substringBefore(" (")
            val producto = listaFiltrada.find { it.nombre == nombreLimpio }
            producto?.let { agregarAlCarrito(it) }
            autoCompleteProducto.text.clear()
        }
    }

    private fun agregarAlCarrito(prod: ProductoResponse) {
        val regla = reglasDescuento.find { it.productoId == prod.id }
            ?: reglasDescuento.find { it.marcaId == prod.marcaId }
            ?: reglasDescuento.find { it.clienteId == clienteSeleccionadoId }

        val porcentaje = regla?.descuentoPorcentaje ?: 0.0
        val precioFinal = prod.precioBase * (1 - (porcentaje / 100))

        // ProductoCarrito usa nombres snake_case para coincidir con backend
        val item = ProductoCarrito(
            producto_id = prod.id,
            nombre = prod.nombre,
            precio_unitario = precioFinal,
            cantidad = 1.0,
            es_granel = prod.esGranel
        )
        adapterCarrito.agregarProducto(item)
        calcularTotal()
    }

    private fun calcularTotal() {
        val total = listaCarrito.sumOf { it.precio_unitario * it.cantidad }
        tvTotal.text = NumberFormat.getCurrencyInstance(Locale.US).format(total)
    }

    private fun cobrarVenta() {
        val prefs = getSharedPreferences("PuntoPeludoPrefs", MODE_PRIVATE)
        val sId = prefs.getInt("ID_SUCURSAL_SESION", 1)
        val uId = prefs.getInt("ID_USUARIO_SESION", 1)
        val corteId = prefs.getInt("corte_caja_id", -1)

        if (corteId == -1) {
            Toast.makeText(this, "❌ Abre caja primero", Toast.LENGTH_LONG).show()
            return
        }

        if (listaCarrito.isEmpty()) return

        lifecycleScope.launch {
            try {
                val totalVenta = listaCarrito.sumOf { it.precio_unitario * it.cantidad }

                // Construcción de VentaIn cumpliendo con schemas.py
                val ventaReq = VentaIn(
                    sucursal_id = sId,
                    usuario_id = uId,
                    cliente_id = clienteSeleccionadoId,
                    corte_caja_id = corteId,
                    total = totalVenta,
                    descuento_especial_monto = 0.0,
                    descuento_especial_motivo = null
                )

                // 1. Crear Venta
                val vRes = RetrofitClient.instance.crearVenta(ventaReq)

                // 2. Registrar Detalles
                listaCarrito.forEach {
                    RetrofitClient.instance.registrarDetalleVenta(VentaDetalleIn(
                        venta_id = vRes.id,
                        producto_id = it.producto_id,
                        cantidad = it.cantidad,
                        precio_unitario = it.precio_unitario
                    ))
                }

                Toast.makeText(this@VentaActivity, "✅ Venta #${vRes.id} Exitosa", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("VENTA_ERROR", "422 Check: ${e.message}")
                Toast.makeText(this@VentaActivity, "❌ Error al procesar cobro (Datos inválidos)", Toast.LENGTH_SHORT).show()
            }
        }
    }
}