package com.example.listapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.activity.addCallback

class LoginFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

           }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val btnForgotPassword = view.findViewById<Button>(R.id.btnForgotPassword)
        val navController = findNavController()

        btnRegister.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_formFragment)
        }

        btnForgotPassword.setOnClickListener{
            navController.navigate(R.id.action_loginFragment_to_passwordRecoveryFragment)
        }

        return view
    }
}