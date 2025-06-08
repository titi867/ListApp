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

/*
Actividad principal que gestiona el splash screen, navegación y autenticación,
redirigiendo al dashboard si hay usuario logueado.
 */
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        /*
        Implementa el splash screen diferenciado: usa la API moderna en Android 12+
        y recurre al tema tradicional en versiones anteriores.

        (Garantiza la transición visual óptima del splash al contenido principal según la versión del SO).
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installSplashScreen() // Android 12+ usa splash API moderna
        } else {
            setTheme(R.style.Theme_ListApp) // Android <= 11 usa el tema estándar después del splash
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        /*
        Obtiene el NavHostFragment y su NavController para gestionar la navegación entre fragments.
         */
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.login_fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController

        /*
        Verifica si hay un usuario autenticado y navega directamente al dashboard si la sesión existe.

        (Redirección automática para usuarios ya logueados que evita repetir autenticación).
         */
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            navController.navigate(R.id.action_loginFragment_to_dashboardFragment)
        }

        /*
        Configura el listener para manejar los system bars (notch, navegación) y ajustar el padding del layout principal.

        (Implementación edge-to-edge que evita solapamiento con áreas del sistema).
         */
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /*
    Maneja la navegación hacia atrás mediante el botón "Up" de la ActionBar, delegando al NavController.

    (Integración con el sistema de navegación que respeta la jerarquía de fragments).
    */
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.login_fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}