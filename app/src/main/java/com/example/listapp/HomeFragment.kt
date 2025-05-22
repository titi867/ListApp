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

        ivAddress.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISO_UBICACION)
            } else {
                abrirMapa()
            }
        }

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

