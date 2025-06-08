package com.example.listapp

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.listapp.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

/*
Fragmento de inicio que maneja interacciones de contacto: llamadas, emails,
ubicación y WhatsApp, con gestión de permisos.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val PERMISO_LLAMADA = 101
    private val PERMISO_UBICACION = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnContact = binding.btnContact
        val ivPhone = binding.ivPhone
        val ivMail = binding.ivMail
        val etPhone = binding.etPhone
        val editTextMail = binding.editTextMail
        val ivAddress = binding.ivAddress
        val ivWhatsapp = binding.ivWhatsapp

        btnContact.setOnClickListener {
            Snackbar.make(it, R.string.sentContact, Snackbar.LENGTH_SHORT).show()
        }

        /*
        Verifica y solicita permiso para llamadas telefónicas al hacer clic,
        ejecutando la llamada si ya está concedido.

        Flujo completo: check permission -> request if needed -> launch call intent.
        */
        ivPhone.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.CALL_PHONE),
                    PERMISO_LLAMADA
                )
            } else {
                realizarllamadaPorTelefono(etPhone.text.toString())
            }
        }

        /*
        Abre el cliente de correo predeterminado con datos prellenados al hacer clic,
        mostrando selector de apps si hay varias disponibles.

        (Configura intent para enviar email con asunto/cuerpo predefinidos
        y maneja fallos de disponibilidad).
         */
        ivMail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply{
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, editTextMail.text.toString())
                putExtra(Intent.EXTRA_SUBJECT, "Correo desde la app")
                putExtra(Intent.EXTRA_TEXT, "Este es un mensaje enviado por la aplicación de Gundam.")
            }

            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(Intent.createChooser(intent, "Enviar email vía... "))
            }
        }

        /*
        Verifica y solicita permiso de ubicación al hacer clic,
        abriendo el mapa si ya está concedido.
         */
        ivAddress.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISO_UBICACION)
            } else {
                abrirMapa()
            }
        }

        /*
        Abre WhatsApp con un número predefinido al hacer clic,
        mostrando error si la app no está instalada.

        (Intento directo de abrir chat de WhatsApp validando
        previamente la disponibilidad de la app).
         */
        ivWhatsapp.setOnClickListener {
            val uri = Uri.parse("https://wa.me/" + etPhone.text.toString())
            val intent = Intent(Intent.ACTION_VIEW,uri)

            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                Snackbar.make(it, "Whatsapp no instalado", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    /*
    Inicia una llamada telefónica directa usando el número proporcionado,
    requiriendo el permiso CALL_PHONE.
     */
    private fun realizarllamadaPorTelefono(phone:String){
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel: " + phone))
        startActivity(intent)
    }

    /*
    Abre la app de mapas predeterminada mostrando la ubicación del Estadio Santiago Bernabéu.
     */
    private fun abrirMapa(){
        val uri = Uri.parse("geo:0,0?q=Estadio+Santiago+Bernabeu")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}

