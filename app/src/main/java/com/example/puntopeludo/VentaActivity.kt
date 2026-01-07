package com.example.puntopeludo

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import java.text.NumberFormat
import java.util.Locale

class VentaActivity : AppCompatActivity() {

    private lateinit var adapterCarrito: VentaAdapter
    private lateinit var tvTotal: TextView
    private lateinit var autoCompleteProducto: AutoCompleteTextView
    private lateinit var chipGroup: ChipGroup

    // --- SIMULACIÓN DE BASE DE DATOS ---
    // En el futuro, esto vendrá de la API
    data class ProductoFake(val nombre: String, val precio: Double, val categoria: String, val esGranel: Boolean)

    private val inventarioCompleto = listOf(
        ProductoFake("Nupec Adulto 20kg", 1450.00, "perro", false),
        ProductoFake("Nupec Cachorro 5kg", 600.00, "perro", false),
        ProductoFake("Collar Antipulgas", 250.00, "farmacia", false),
        ProductoFake("Whiskas Atún", 12.00, "gato", false),
        ProductoFake("Arena para Gato", 80.00, "gato", false),
        ProductoFake("Maíz Quebrado", 10.50, "granel", true),
        ProductoFake("Alimento Pollo Inicio", 15.00, "granel", true),
        ProductoFake("Vitaminas Inyectables", 120.00, "farmacia", false)
    )

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
        configurarCarrito(rvCarrito)

        // 3. Configurar Filtros (Chips)
        configurarFiltros()

        // 4. Configurar Buscador Inicial (Mostrar todos)
        actualizarBuscador("todos")

        // 5. Botón Cobrar
        btnFinalizar.setOnClickListener { cobrarVenta() }
    }

    private fun configurarCarrito(recyclerView: RecyclerView) {
        adapterCarrito = VentaAdapter {
            calcularTotal() // Si borran algo, recalcula
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapterCarrito
    }

    private fun configurarFiltros() {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            // checkedIds es una List<Int> (lista de IDs seleccionados)

            // Si es selección única (single selection), tomas el primer elemento:
            if (checkedIds.isNotEmpty()) {
                val checkedId = checkedIds[0]
                // Tu lógica aquí usando checkedId
            } else {
                // Caso cuando no hay nada seleccionado (si aplica)
            }
        }
    }

    private fun actualizarBuscador(filtro: String) {
        // 1. Filtrar la lista falsa según el chip seleccionado
        val listaFiltrada = if (filtro == "todos") {
            inventarioCompleto
        } else {
            inventarioCompleto.filter { it.categoria == filtro || (filtro == "granel" && it.esGranel) }
        }

        // 2. Crear lista de nombres para el AutoComplete
        val nombresProductos = listaFiltrada.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombresProductos)
        autoCompleteProducto.setAdapter(adapter)

        // 3. Escuchar Clics en el Buscador
        autoCompleteProducto.setOnItemClickListener { _, _, position, _ ->
            // Buscar el objeto completo basado en el nombre seleccionado
            val nombreSeleccionado = adapter.getItem(position)
            val productoReal = listaFiltrada.find { it.nombre == nombreSeleccionado }

            if (productoReal != null) {
                agregarAlCarrito(productoReal)
                autoCompleteProducto.text.clear() // Limpiar para la siguiente búsqueda
            }
        }
    }

    private fun agregarAlCarrito(prod: ProductoFake) {
        // Convertir nuestro producto fake al modelo del carrito
        val itemCarrito = ProductoCarrito(
            idProducto = (1..9999).random(), // ID Temporal
            nombre = prod.nombre,
            precioUnitario = prod.precio,
            cantidad = 1.0, // Por defecto 1 pieza o 1 kilo
            esGranel = prod.esGranel
        )

        adapterCarrito.agregarProducto(itemCarrito)
        calcularTotal()
    }

    private fun calcularTotal() {
        val productos = adapterCarrito.obtenerLista()
        var total = 0.0
        for (p in productos) {
            total += p.calcularSubtotal()
        }
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        tvTotal.text = format.format(total)
    }

    private fun cobrarVenta() {
        val productos = adapterCarrito.obtenerLista()
        if (productos.isEmpty()) {
            Toast.makeText(this, "Carrito vacío", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "Enviando venta al servidor...", Toast.LENGTH_LONG).show()
        // Aquí iría la conexión con Retrofit
        adapterCarrito.limpiarCarrito()
        calcularTotal()
    }
}