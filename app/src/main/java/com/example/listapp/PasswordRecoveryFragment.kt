package com.example.listapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.activity.addCallback
import com.example.listapp.databinding.FragmentPasswordRecoveryBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

/*
Fragmento para recuperación de contraseña: valida email y envía link de reset via Firebase Auth,
con navegación automática al login.

(Flujo completo: validación de formato → envío de email → feedback visual → redirección post éxito).
 */
class PasswordRecoveryFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentPasswordRecoveryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
        binding = FragmentPasswordRecoveryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        Valida el email ingresado y, si es válido, envía un correo de recuperación de
        contraseña mediante Firebase Auth, mostrando notificaciones de éxito/error.

        (Flujo completo: verificación de formato -> envío de email -> feedback visual -> navegación automática al login tras 2 segundos si es exitoso).
         */
        binding.btnResetPassword.setOnClickListener {
            val email = binding.etRecoveryEmail.text.toString().trim()
            if(!isValidEmail(email)){
                Snackbar.make(view, "Enter a valid email", Snackbar.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task->
                    if(task.isSuccessful) {
                        Snackbar.make(view, "A password recovery email has been sent", Snackbar.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            findNavController().navigate(R.id.action_passwordRecoveryFragment_to_loginFragment)
                        },2000)
                    } else {
                        Snackbar.make(view, "Error while sending the password recovery email", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun isValidEmail(email:String): Boolean{
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}