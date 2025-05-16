package com.example.listapp

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar

class ContactoActivity : AppCompatActivity() {

    private val PERMISO_LLAMADA = 101
    private val PERMISO_UBICACION = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.layout_contacto)

        val btnContact = findViewById<Button>(R.id.btnContact)
        val ivPhone = findViewById<ImageView>(R.id.ivPhone)
        val ivMail = findViewById<ImageView>(R.id.ivMail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val editTextMail = findViewById<EditText>(R.id.editTextMail)
        val ivAddress = findViewById<ImageView>(R.id.ivAddress)
        val ivWhatsapp = findViewById<ImageView>(R.id.ivWhatsapp)

        btnContact.setOnClickListener {

            Snackbar.make(it, R.string.sentContact, Snackbar.LENGTH_SHORT).show()
        }

        ivPhone.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CALL_PHONE),
                    PERMISO_LLAMADA
                )
            } else {
                realizarllamadaPorTelefono(etPhone.text.toString())
            }
        }

        ivMail.setOnClickListener {
           val intent = Intent(Intent.ACTION_SEND).apply{
               type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, editTextMail.text.toString())
                putExtra(Intent.EXTRA_SUBJECT, "Correo desde la app")
                putExtra(Intent.EXTRA_TEXT, "Este es un mensaje enviado por la aplicación de Gundam.")
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(intent, "Enviar email vía... "))
            }
        }

        ivAddress.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISO_UBICACION)
            } else {
                abrirMapa()
            }
        }

        ivWhatsapp.setOnClickListener {
            val uri = Uri.parse("https://wa.me/" + etPhone.text.toString())
            val intent = Intent(Intent.ACTION_VIEW,uri)

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Snackbar.make(it, "Whatsapp no instalado", Snackbar.LENGTH_SHORT).show()
            }
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun realizarllamadaPorTelefono(phone:String){
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel: " + phone))
        startActivity(intent)
    }

    private fun abrirMapa(){

        val uri = Uri.parse("geo:0,0?q=Estadio+Santiago+Bernabeu")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}