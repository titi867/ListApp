package com.example.listapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.example.listapp.databinding.FragmentFormBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class FormFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding = FragmentFormBinding.inflate(layoutInflater)
        return binding.root

        return inflater.inflate(R.layout.fragment_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivPhoto = view.findViewById<ImageView>(R.id.ivPhoto)
        Glide.with(requireContext()).load("https://goo.gl/gEgYUd").into(ivPhoto)

        val btnFormSignUp = binding.btnFormSignUp

        btnFormSignUp.setOnClickListener {
            val email = binding.etFormEmail.text.toString().trim()
            val password = binding.etFormPassword.text.toString().trim()

            val nickname = binding.etNickname.text.toString().trim()
            var birthDate = binding.etBirthday.text.toString().trim()

            //validación de los campos
            //falta validación del correo
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userId = user?.uid
                        val userMap = hashMapOf(
                            "email" to email,
                            "nickname" to nickname,
                            "birthday" to birthDate
                        )

                        if (userId != null) {
                            firestore.collection("users").document(userId).set(userMap).addOnSuccessListener {
                                Snackbar.make(view, "Usuario creado exitosamente", Snackbar.LENGTH_SHORT).show()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    findNavController().navigate(R.id.action_formFragment_to_loginFragment)
                                }, 2000)
                            }.addOnFailureListener {
                                Snackbar.make(view, "Error al guardar datos de usuario", Snackbar.LENGTH_SHORT).show()
                            }
                        }

                    } else {
                        Snackbar.make(it, "Error al crear usuario", Snackbar.LENGTH_SHORT).show()
                    }

                }
            } else {
                Snackbar.make(it, "Ingresa el usuario y contraseña", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}