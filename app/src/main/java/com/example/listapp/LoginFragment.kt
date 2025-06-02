package com.example.listapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.activity.addCallback
import com.example.listapp.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_formFragment)
        }

        binding.btnForgotPassword.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_passwordRecoveryFragment)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etUsuario.text.toString().trim()
            val password = binding.etContrasena.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()){
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val userId = auth.currentUser?.uid
                        if(userId != null) {
                            firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                                if(document != null && document.exists()){
                                    val nickname = document.getString("nickname")
                                    val message = "Bienvenido, $nickname!!"
                                    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                                }
                            }.addOnFailureListener {
                                Snackbar.make(view, "Datos del usuario no encontrados", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        val TAG = "LoginFragment"
                        android.util.Log.e(TAG, "Login failed", task.exception)
                        Snackbar.make(view, "Login failed", Snackbar.LENGTH_SHORT).show()
                    }
                }
            } else {
                Snackbar.make(view, "Please enter email and password", Snackbar.LENGTH_SHORT).show()
            }


        }
//ray.esro@gmail.com

    }

}