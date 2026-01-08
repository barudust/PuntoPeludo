package com.example.puntopeludo

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class VentaActivity : AppCompatActivity() {

    private lateinit var adapterCarrito: VentaAdapter
    private lateinit var tvTotal: TextView
    private lateinit var autoCompleteProducto: AutoCompleteTextView
    private lateinit var chipGroup: ChipGroup

    // Listas reales que vienen del servidor
    private var inventarioReal = mutableListOf<Producto>()
    private var reglasDescuento = mutableListOf<ReglaDescuento>()
    private var clienteSeleccionadoId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta)

        // 1. Vincular Vistas
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        autoCompleteProducto = findViewById(R.id.autoCompleteProducto)
        tvTotal = findViewById(R.id.textViewTotal)
        chipGroup = findViewById(R.id.chipGroupFiltros)
        val btnFinalizar = findViewById<MaterialButton>(R.id.buttonFinalizarVenta)
        val rvCarrito = findViewById<RecyclerView>(R.id.recyclerViewCarrito)

        btnBack.setOnClickListener { finish() }

        // 2. Configurar Carrito
        adapterCarrito = VentaAdapter { calcularTotal() }
        rvCarrito.layoutManager = LinearLayoutManager(this)
        rvCarrito.adapter = adapterCarrito

        // 3. Cargar datos del servidor
        cargarDatosServidor()

        // 4. Botón Cobrar
        btnFinalizar.setOnClickListener { cobrarVenta() }
    }

    private fun cargarDatosServidor() {
        lifecycleScope.launch {
            try {
                // Obtenemos productos y descuentos reales
                inventarioReal = RetrofitClient.instance.getProductos().toMutableList()
                reglasDescuento = RetrofitClient.instance.getDescuentos().toMutableList()

                actualizarBuscador("todos")
            } catch (e: Exception) {
                Log.e("VENTA_ERROR", "Error: ${e.message}")
                Toast.makeText(this@VentaActivity, "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarBuscador(filtro: String) {
        val listaFiltrada = if (filtro == "todos") {
            inventarioReal
        } else {
            // Usamos tipo_producto que definimos en el paso anterior
            inventarioReal.filter { it.tipoProducto?.lowercase() == filtro.lowercase()}
        }

        val nombres = listaFiltrada.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombres)
        autoCompleteProducto.setAdapter(adapter)

        autoCompleteProducto.setOnItemClickListener { _, _, position, _ ->
            val nombreSel = adapter.getItem(position)
            val producto = listaFiltrada.find { it.nombre == nombreSel }
            producto?.let { agregarAlCarrito(it) }
            autoCompleteProducto.text.clear()
        }
    }

    private fun agregarAlCarrito(prod: Producto) {
        // Usamos los nombres de ReglaDescuento (productoId, marcaId, etc.)
        val regla = reglasDescuento.find { it.productoId == prod.id }
            ?: reglasDescuento.find { it.marcaId == prod.marcaId }
            ?: reglasDescuento.find { it.clienteId == clienteSeleccionadoId }

        val porcentaje = regla?.descuentoPorcentaje ?: 0.0
        // Usamos prod.precioBase (que ya existe en tu ProductoResponse)
        val precioFinal = prod.precioBase * (1 - (porcentaje / 100))

        val item = ProductoCarrito(
            idProducto = prod.id,
            nombre = prod.nombre,
            precioUnitario = precioFinal,
            cantidad = 1.0,
            esGranel = prod.esGranel // Usamos esGranel que ya tienes en ProductoResponse
        )
        adapterCarrito.agregarProducto(item)
        calcularTotal()
    }

    private fun calcularTotal() {
        val total = adapterCarrito.obtenerLista().sumOf { it.precioUnitario * it.cantidad }
        tvTotal.text = NumberFormat.getCurrencyInstance(Locale.US).format(total)
    }

    private fun cobrarVenta() {
        val prefs = getSharedPreferences("PuntoPeludoPrefs", MODE_PRIVATE)
        val corteId = prefs.getInt("corte_caja_id", -1)

        if (corteId == -1) {
            Toast.makeText(this, "❌ Debes abrir caja antes de vender", Toast.LENGTH_LONG).show()
            return
        }

        val listaCarrito = adapterCarrito.obtenerLista()
        if (listaCarrito.isEmpty()) {
            Toast.makeText(this, "Carrito vacío", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Calculamos el total
                val totalVenta = listaCarrito.sumOf { it.precioUnitario * it.cantidad }

                // 1. Crear la cabecera de la venta
                val ventaReq = VentaIn(
                    sucursal_id = prefs.getInt("sucursal_id", 1),
                    usuario_id = prefs.getInt("usuario_id", 1),
                    cliente_id = clienteSeleccionadoId,
                    corte_caja_id = corteId,
                    total = totalVenta
                )

                val ventaRealizada = RetrofitClient.instance.crearVenta(ventaReq)

                // 2. Registrar cada producto en venta_detalle
                listaCarrito.forEach { item ->
                    val detalle = VentaDetalleIn(
                        venta_id = ventaRealizada.id,
                        producto_id = item.idProducto,
                        cantidad = item.cantidad,
                        precio_unitario = item.precioUnitario
                    )
                    RetrofitClient.instance.registrarDetalleVenta(detalle)
                }

                Toast.makeText(this@VentaActivity, "✅ Venta #\${ventaRealizada.id} Exitosa", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("VENTA_ERROR", "Error al cobrar: \${e.message}")
                Toast.makeText(this@VentaActivity, "❌ Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
            }
        }
    }



}