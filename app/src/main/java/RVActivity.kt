package com.example.listapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listapp.R
import com.example.listapp.adapters.GundamAdapter
import com.example.listapp.models.Gundam

class RVActivity : AppCompatActivity() {
    private fun gundam() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rvactivity)

        // Datos de prueba
            val gundamList = listOf (

                Gundam(1, "Kimaris", "Unit", R.drawable.kimaris),
                Gundam(2, "Aerial", "Unit", R.drawable.aerial),
                Gundam(3, "Fenice Rinascita", "Unit", R.drawable.fenice_rinascita),
                Gundam(4, "Shin Musha", "Unit", R.drawable.shin_musha),
                Gundam(5, "Astray", "Unit", R.drawable.astray),
                Gundam(6, "Duel Blitz", "Unit", R.drawable.duel_blitz),
                Gundam(7, "Gunpla", "Unit", R.drawable.gunpla),
                Gundam(8, "Heavy Arms", "Unit", R.drawable.heavy_arms)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.rvItems)
        //define la estructura de cómo se va a estructurar la lista
        recyclerView.layoutManager = LinearLayoutManager(this)
        //define la estructura individual de los elementos de la lista
        //adaptando la información de los elementos de la lista a los views
        recyclerView.adapter = GundamAdapter(gundamList)

        val btnContact = findViewById<Button>(R.id.btnContacto)
        btnContact.setOnClickListener{
            val intent = Intent(this, ContactoActivity::class.java)
            startActivity(intent)
        }

       ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerViewLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
              }
    }
}