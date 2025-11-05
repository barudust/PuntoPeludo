package com.example.puntopeludo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class VentaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simplemente cargamos el diseño XML que hicimos.
        // Ya no hay 'enableEdgeToEdge' ni 'ViewCompat' buscando 'R.id.main'
        setContentView(R.layout.activity_venta)

        // (En el futuro, aquí pondremos la lógica para los spinners
        // y botones de ESTA pantalla, pero por ahora, solo la mostramos.)
    }
}