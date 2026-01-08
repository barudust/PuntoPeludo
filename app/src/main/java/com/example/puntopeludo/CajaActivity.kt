package com.example.puntopeludo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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
        val inputEfectivo = EditText(this).apply {
            hint = "Efectivo físico contado"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar Caja")
            .setMessage("Ingresa cuánto dinero hay físicamente en caja:")
            .setView(inputEfectivo)
            .setPositiveButton("Cerrar Turno") { _, _ ->
                val contado = inputEfectivo.text.toString().toDoubleOrNull() ?: 0.0
                efectuarCierre(contado)
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
        findViewById<View>(R.id.cardAbrirCaja).visibility = if (abierta) View.GONE else View.VISIBLE
        findViewById<View>(R.id.cardResumenCaja).visibility = if (abierta) View.VISIBLE else View.GONE

        corte?.let {
            findViewById<TextView>(R.id.tvVentasTotales).text = "Ventas acumuladas: $${it.ventas_totales}"
            findViewById<TextView>(R.id.tvEfectivoEsperado).text = "Total esperado: $${it.efectivo_esperado}"
        }
    }
}