package com.example.puntopeludo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class InventarioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simplemente cargamos el diseño XML que hicimos.
        // Ya no hay 'findViewById(R.id.main)'
        setContentView(R.layout.activity_inventario)

        // (En el siguiente paso, aquí es donde
        //  llamaremos a la API y configuraremos el RecyclerView)
    }
}