package com.example.puntopeludo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch


class CajaActivity : AppCompatActivity() {

    private var idCorteActual: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caja)

        checkEstadoCaja()

        findViewById<Button>(R.id.btnAbrirTurno).setOnClickListener { realizarApertura() }
        findViewById<Button>(R.id.btnCerrarTurno).setOnClickListener { mostrarDialogoCierre() }
    }


    private fun checkEstadoCaja() {
        val prefs = getSharedPreferences("PuntoPeludoPrefs", MODE_PRIVATE)
        val usuarioId = prefs.getInt("ID_USUARIO_SESION", -1)

        if (usuarioId == -1) {
            Toast.makeText(this, "Sesión no encontrada. Reingresa.", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Esto llamará a GET /corte/actual/{usuarioId}
                val corte = RetrofitClient.instance.getCorteActual(usuarioId)
                idCorteActual = corte.id
                actualizarUI(true, corte)

                // Guardamos el ID del corte para la venta
                prefs.edit().putInt("corte_caja_id", corte.id).apply()
            } catch (e: Exception) {
                actualizarUI(false, null)
            }
        }
    }

    private fun realizarApertura() {
        val prefs = getSharedPreferences("PuntoPeludoPrefs", MODE_PRIVATE)
        val uId = prefs.getInt("ID_USUARIO_SESION", -1)
        val sId = prefs.getInt("ID_SUCURSAL_SESION", -1)

        val fondo = findViewById<EditText>(R.id.etFondoInicial).text.toString().toDoubleOrNull() ?: 0.0

        lifecycleScope.launch {
            try {
                val req = AperturaCajaReq(
                    sucursal_id = sId, //
                    usuario_id = uId,   //
                    fondo_inicial = fondo
                )
                RetrofitClient.instance.abrirCaja(req)
                checkEstadoCaja() // Recargar pantalla
            } catch (e: Exception) {
                Toast.makeText(this@CajaActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoCierre() {
        // Solo un campo para el efectivo contado
        val inputEfectivo = EditText(this).apply {
            hint = "Dinero total en caja ($)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(50, 40, 50, 40)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar Turno")
            .setMessage("Ingresa el total de efectivo que hay en la gaveta.")
            .setView(inputEfectivo)
            .setPositiveButton("Cerrar Caja") { _, _ ->
                val contado = inputEfectivo.text.toString().toDoubleOrNull() ?: 0.0
                ejecutarCierreBackend(contado)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarCierreBackend(fisico: Double) {
        val corteId = idCorteActual ?: return

        lifecycleScope.launch {
            try {
                val req = CierreCajaReq(
                    corte_id = corteId,
                    efectivo_real = fisico, // Lo que el cajero contó
                    monto_retirado = 0.0    // Enviamos 0 por ahora para simplificar
                )

                val resumen = RetrofitClient.instance.cerrarCaja(req)

                // Mostrar si hubo descuadre
                val diferencia = resumen.diferencia ?: 0.0
                val mensaje = when {
                    diferencia == 0.0 -> "✅ Caja cuadrada perfectamente."
                    diferencia > 0.0 -> "✅ Caja cerrada. Sobrante: $$diferencia"
                    else -> "⚠️ Caja cerrada con FALTANTE: $$diferencia"
                }

                Toast.makeText(this@CajaActivity, mensaje, Toast.LENGTH_LONG).show()

                // Limpiar sesión de caja local
                getSharedPreferences("PuntoPeludoPrefs", MODE_PRIVATE).edit()
                    .remove("corte_caja_id").apply()

                checkEstadoCaja() // Volver al estado de "Abrir Caja"

            } catch (e: Exception) {
                Toast.makeText(this@CajaActivity, "Error al conectar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun efectuarCierre(contado: Double) {
        val corteId = idCorteActual ?: return
        lifecycleScope.launch {
            try {
                val req = CierreCajaReq(
                    corte_id = corteId,
                    efectivo_real = contado, //
                    monto_retirado = 0.0 // Aquí podrías preguntar cuánto se retira
                )
                RetrofitClient.instance.cerrarCaja(req)
                Toast.makeText(this@CajaActivity, "Turno cerrado exitosamente", Toast.LENGTH_SHORT).show()
                checkEstadoCaja()
            } catch (e: Exception) {
                Toast.makeText(this@CajaActivity, "Error al cerrar caja", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarUI(abierta: Boolean, corte: CorteResponse?) {
        // 1. Visibilidad de las tarjetas
        findViewById<View>(R.id.cardAbrirCaja).visibility = if (abierta) View.GONE else View.VISIBLE
        findViewById<View>(R.id.cardResumenCaja).visibility = if (abierta) View.VISIBLE else View.GONE

        corte?.let {
            // Usamos Locale.US para que el signo de $ salga siempre bien
            val formatMoneda = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US)

            // --- FECHA Y HORA (Lo que ya funcionaba) ---
            val rawFecha = it.fecha_apertura ?: ""
            findViewById<TextView>(R.id.tvFechaActual).text = if (rawFecha.length >= 10) rawFecha.take(10) else "---"

            try {
                // Buscamos la hora después de la T o el espacio
                val horaLimpia = if (rawFecha.contains("T")) {
                    rawFecha.substringAfter("T").take(5)
                } else if (rawFecha.contains(" ")) {
                    rawFecha.substringAfter(" ").take(5)
                } else {
                    "--:--"
                }
                findViewById<TextView>(R.id.tvHoraInicio).text = "Desde las $horaLimpia"
            } catch (e: Exception) {
                findViewById<TextView>(R.id.tvHoraInicio).text = "En curso"
            }

            // --- LOS NÚMEROS (Lo que se "rompió") ---
            // Forzamos la conversión a Double por si llegan como String del servidor
            try {
                val fondo = it.fondo_inicial.toDouble()
                val ventas = it.ventas_totales.toDouble()
                val esperado = it.efectivo_esperado.toDouble()

                findViewById<TextView>(R.id.tvFondoInicial).text = formatMoneda.format(fondo)
                findViewById<TextView>(R.id.tvVentasTotales).text = formatMoneda.format(ventas)
                findViewById<TextView>(R.id.tvEfectivoEsperado).text = formatMoneda.format(esperado)
            } catch (e: Exception) {
                // Si falla el formato, al menos ponemos el número crudo para que no salga vacío
                findViewById<TextView>(R.id.tvFondoInicial).text = "$${it.fondo_inicial}"
                findViewById<TextView>(R.id.tvVentasTotales).text = "$${it.ventas_totales}"
                findViewById<TextView>(R.id.tvEfectivoEsperado).text = "$${it.efectivo_esperado}"
            }
        }
    }
}