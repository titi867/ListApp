package com.example.listapp

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import com.example.listapp.RVActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

//        /*
//        Aquí extraemos el ID de los view de tipo TextInputEditText.
//        En los layouts usamos la combinación de TextInputLayout con TextInputEditText que son los que tiene la funcionalidad integrada
//        de mostrar errores, así como mostar los hints dentro del TextInputEditText cuando éste esta vacío y, luego de ingresar datos,
//        el hint pasa encima del TextInputEditText. Esto nos facilita el mostrar errores, por ende, mejora la usabilidad de la app
//         */
//
//        val etUsuario = findViewById<TextInputEditText>(R.id.etUsuario)
//        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasena)
//
//        // Aquí extraemos los Ids de los Views de tipo botones
//
//        val btnLogin = findViewById<Button>(R.id.btnLogin)
//        val btnRegister = findViewById<Button>(R.id.btnRegister)
//        val btnForgotPassword =findViewById<Button>(R.id.btnForgotPassword)
//        val btnFacebook = findViewById<Button>(R.id.btnFacebook)
//        val btnGoogle = findViewById<Button>(R.id.btnGoogle)
//
//        // Aquí definimos el comportamiento del click en el botón Login usando la función setOnClickListener.
//        // El código que está dentro del setOnClickListener se ejecutarà al darle clic al botón (btnLogin en este caso)
//        btnLogin.setOnClickListener {
//            if(etUsuario.text.isNullOrEmpty() || etContrasena.text.isNullOrEmpty() )
//            {
//                if (etUsuario.text.isNullOrEmpty()) {
//                    findViewById<TextInputLayout>(R.id.textInputLayoutUsuario).error = getString(R.string.userNameRequired)
//                    //Snackbar.make(it, "El usuario es requerido", Snackbar.LENGTH_SHORT).show()}
//                }
//                if (etContrasena.text.isNullOrEmpty()) {
//                    findViewById<TextInputLayout>(R.id.textInputLayoutContrasena).error = getString(R.string.passwordRequired)
//                    //Snackbar.make(it, "La contraseña es requerida", Snackbar.LENGTH_SHORT).show()
//                }
//            }
//            else{
//                findViewById<TextInputLayout>(R.id.textInputLayoutUsuario).error = ""
//                findViewById<TextInputLayout>(R.id.textInputLayoutContrasena).error = ""
//                Snackbar.make(it, R.string.welcome, Snackbar.LENGTH_SHORT).show()
//
//                //Los intent sirven para lanzar diferentes activities en una app
//                val intent = Intent(this, RVActivity::class.java)
//                startActivity(intent)
//            }
//        }
//
//        // De aquí para abajo definimos el comportamiento del evento click en el resto de los botones
//        btnRegister.setOnClickListener {
//            //Mostrar un snackbar
//            Snackbar.make(it, R.string.userRegistered, Snackbar.LENGTH_SHORT).show()
//        }
//
//        btnForgotPassword.setOnClickListener{
//            //Mostrar un snackbar
//            Snackbar.make(it, R.string.recoverPasswordSent, Snackbar.LENGTH_SHORT).show()
//            }
//
//        btnFacebook.setOnClickListener{
//            //Mostrar un snackbar
//            Snackbar.make(it, R.string.loggingInWithFacebook, Snackbar.LENGTH_SHORT).show()
//        }
//
//        btnGoogle.setOnClickListener{
//            //Mostrar un snackbar
//            Snackbar.make(it, R.string.loggingInWithGoogle, Snackbar.LENGTH_SHORT).show()
//        }

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