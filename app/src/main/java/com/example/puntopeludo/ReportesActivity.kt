package com.example.puntopeludo

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.text.NumberFormat // <--- ESTA ERA LA LÍNEA QUE FALTABA
import java.util.*

class ReportesActivity : AppCompatActivity() {

    private lateinit var tvRango: TextView
    private lateinit var rv: RecyclerView
    private var tabSeleccionado = 0 // 0=Ventas, 1=Surtidos, 2=Cortes

    // Fechas por defecto: HOY
    private var fechaInicioStr: String = ""
    private var fechaFinStr: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)

        // Init Fechas (Hoy)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val hoy = Date()
        fechaInicioStr = sdf.format(hoy)
        fechaFinStr = sdf.format(hoy)

        initViews()
        cargarDatos()
    }

    private fun initViews() {
        // Toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbarReportes)
        toolbar.setNavigationOnClickListener { finish() }

        // Fechas
        tvRango = findViewById(R.id.tvRangoFechas)
        tvRango.text = "Mostrando: Hoy ($fechaInicioStr)"

        findViewById<android.view.View>(R.id.btnCambiarFecha).setOnClickListener {
            mostrarSelectorFechas()
        }

        // Tabs
        val tabs = findViewById<TabLayout>(R.id.tabLayoutReportes)
        tabs.addTab(tabs.newTab().setText("Ventas"))
        tabs.addTab(tabs.newTab().setText("Surtidos"))
        tabs.addTab(tabs.newTab().setText("Cortes"))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tabSeleccionado = tab?.position ?: 0
                cargarDatos()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Recycler
        rv = findViewById(R.id.rvReportes)
        rv.layoutManager = LinearLayoutManager(this)
    }

    private fun mostrarSelectorFechas() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("Selecciona rango")
        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            // Ajuste de zona horaria simple
            val f1 = Date(selection.first)
            val f2 = Date(selection.second)

            fechaInicioStr = sdf.format(f1)
            fechaFinStr = sdf.format(f2)

            tvRango.text = "Del $fechaInicioStr al $fechaFinStr"
            cargarDatos()
        }
        picker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun cargarDatos() {
        val prefs = getSharedPreferences("PuntoPeludoPrefs", MODE_PRIVATE)
        val sucursalId = prefs.getInt("ID_SUCURSAL_SESION", 1)

        lifecycleScope.launch {
            try {
                rv.adapter = null // Limpiar mientras carga

                when (tabSeleccionado) {
                    0 -> { // VENTAS
                        val datos = RetrofitClient.instance.getReporteVentas(sucursalId, fechaInicioStr, fechaFinStr)
                        rv.adapter = AdapterVentas(datos)
                    }
                    1 -> { // SURTIDOS
                        val datos = RetrofitClient.instance.getReporteSurtidos(sucursalId, fechaInicioStr, fechaFinStr)
                        rv.adapter = AdapterSurtidos(datos)
                    }
                    2 -> { // CORTES
                        val datos = RetrofitClient.instance.getReporteCortes(sucursalId, fechaInicioStr, fechaFinStr)
                        rv.adapter = AdapterCortes(datos)
                    }
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@ReportesActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- ADAPTERS SIMPLES ---

    // 1. VENTAS
    class AdapterVentas(private val lista: List<ReporteVentaItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        class VentaHolder(v: TextView) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val tv = TextView(parent.context)
            tv.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,20) }
            tv.setPadding(30, 30, 30, 30)
            tv.setBackgroundColor(Color.WHITE)
            tv.elevation = 4f
            return VentaHolder(tv)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = lista[position]
            val tv = holder.itemView as TextView
            val cliente = item.cliente ?: "Público General"

            // AQUÍ ES DONDE DABA EL ERROR ANTES
            val total = NumberFormat.getCurrencyInstance(Locale.US).format(item.total)

            tv.text = "Venta #${item.id} - ${item.fecha}\nCliente: $cliente\nTotal: $total (Desc: $${item.descuento})"
            if (item.descuento > 0) tv.setTextColor(Color.parseColor("#1B5E20"))
            else tv.setTextColor(Color.BLACK)
        }
        override fun getItemCount() = lista.size
    }

    // 2. SURTIDOS

    // En ReportesActivity.kt -> Dentro de la clase ReportesActivity

    // 2. SURTIDOS (AGRUPADOS POR BLOQUE)
    class AdapterSurtidos(private val lista: List<ReporteSurtidoBloque>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        class Holder(v: TextView) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val tv = TextView(parent.context)
            // Estilo tarjeta blanca con sombra
            val params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 24) // Espacio entre bloques
            tv.layoutParams = params
            tv.setPadding(40, 40, 40, 40)
            tv.setBackgroundColor(Color.WHITE)
            tv.elevation = 6f
            tv.textSize = 14f
            tv.setTextColor(Color.BLACK)
            return Holder(tv)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val bloque = lista[position]
            val tv = holder.itemView as TextView

            // Construimos el texto del bloque
            val sb = StringBuilder()

            // Cabecera: FECHA y USUARIO
            sb.append("📅 ${bloque.fecha}\n")
            sb.append("👤 Por: ${bloque.usuario}\n")
            sb.append("--------------------------------------\n")

            // Cuerpo: Lista de productos
            bloque.items.forEach { item ->
                // Ejemplo: "• 5.0 kg - Nupec Adulto"
                sb.append("• ${item.cantidad} ${item.unidad} - ${item.producto}\n")
            }

            tv.text = sb.toString()
        }

        override fun getItemCount() = lista.size
    }

    // 3. CORTES
    class AdapterCortes(private val lista: List<ReporteCorteItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        class Holder(v: TextView) : RecyclerView.ViewHolder(v)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val tv = TextView(parent.context)
            tv.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,20) }
            tv.setPadding(30, 30, 30, 30)
            tv.setBackgroundColor(Color.WHITE)
            tv.elevation = 4f
            return Holder(tv)
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = lista[position]
            val tv = holder.itemView as TextView
            val dif = item.diferencia ?: 0.0
            val color = if (dif < 0) "#D32F2F" else if (dif > 0) "#388E3C" else "#000000"

            tv.text = "Corte #${item.id} - ${item.usuario}\nApertura: ${item.fechaApertura}\nVentas Sistema: $${item.ventas}\nDiferencia: $${dif}"
            tv.setTextColor(Color.parseColor(color))
        }
        override fun getItemCount() = lista.size
    }
}