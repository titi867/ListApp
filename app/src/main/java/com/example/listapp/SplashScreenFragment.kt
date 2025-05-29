package com.example.listapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                findNavController().navigate(R.id.action_splashScreenFragment_to_loginFragment)
            } else {
                findNavController().navigate(R.id.action_splashScreenFragment_to_dashboardFragment)

            }
        }, 2000)
    }
}