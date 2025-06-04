package com.example.listapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //Aqui validamos la version del dispositivo para mostrar elsplashscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installSplashScreen() // Android 12+ usa splash API moderna
        } else {
            setTheme(R.style.Theme_ListApp) // Android <= 11 usa el tema estándar después del splash
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.login_fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            navController.navigate(R.id.action_loginFragment_to_dashboardFragment)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.login_fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController

        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}