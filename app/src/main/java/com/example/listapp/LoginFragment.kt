package com.example.listapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.listapp.databinding.FragmentLoginBinding
import com.example.listapp.models.LoginViewModel
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

/*
Fragmento de login que maneja autenticación por email/contraseña y Google Sign-In,
con navegación a otros flujos.

(Gestiona credenciales, validación de formularios, y redirección al dashboard tras login exitoso).
*/
class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        /*
        Configura las opciones de Google Sign-In solicitando token ID, email y usando el client ID de la app.
        (Prepara los parámetros necesarios para la autenticación con Google).
         */
        //TODO cambiar aqui tu token de firebase
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1023194829429-l8igpnhqccomtusnl59r1iv31n1vo5qn.apps.googleusercontent.com")
            .requestEmail()
            .build()

        /*
        Crea el cliente de Google Sign-In usando las opciones configuradas y el contexto de la actividad.

        (Inicializa el objeto que gestionará el proceso de autenticación con Google)
         */
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        /*
        Configura el lanzador para el resultado de inicio de sesión con Google, manejando éxito, cancelación y errores.

        (Procesa la respuesta de la actividad de Google Sign-In y autentica con Firebase al tener éxito).
         */
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                android.app.Activity.RESULT_OK -> {
                    //Obtiene la tarea asíncrona para recuperar la cuenta de Google desde los datos del intent de resultado.
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        /*
                        Obtiene la cuenta de Google autenticada (lanzando excepción si falla) y ejecuta la autenticación
                        en Firebase con el token ID.
                        (Proceso seguro que convierte el resultado de Google Sign-In en credenciales Firebase).
                         */
                        val account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {
                        Snackbar.make(binding.root, "Fallo Google Sign-In: ${e.message}", Snackbar.LENGTH_SHORT).show()
                    }
                }
                android.app.Activity.RESULT_CANCELED -> {
                    Snackbar.make(binding.root, "Google Sign-In cancelado", Snackbar.LENGTH_SHORT).show()
                }
                else -> {
                    Snackbar.make(binding.root, "Error desconocido en Google Sign-In", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        Observa la validación del formulario de login: si es válido ejecuta el login, sino muestra errores en los campos.

        (Reacción dinámica a cambios en el estado de validación del ViewModel).
         */
        loginViewModel.loginFormValid.observe(viewLifecycleOwner, Observer { isValid ->
            if (isValid) {
                val email = binding.etUsuario.text.toString().trim()
                val password = binding.etContrasena.text.toString().trim()
                performLogin(email, password, view)
            } else {
                binding.etUsuario.error = "El usuario es requerido"
                binding.etContrasena.error = "La contraseña es requerido"
                Snackbar.make(view, "Por favor ingresa correo y contraseña", Snackbar.LENGTH_SHORT).show()
            }
        })

        /*
        Ejecuta la validación de credenciales al hacer clic en el botón de login, enviando email y contraseña al ViewModel.

        (Inicia el proceso de validación del formulario cuando el usuario intenta iniciar sesión)
         */
        binding.btnLogin.setOnClickListener {
            val email = binding.etUsuario.text.toString()
            val password = binding.etContrasena.text.toString()
            loginViewModel.validateCredentials(email, password)
        }

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_formFragment)
        }

        binding.btnForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_passwordRecoveryFragment)
        }

        /*
        Inicia el flujo de autenticación con Google al hacer clic, lanzando la actividad de selección de cuenta.

        (Dispara el proceso de Google Sign-In usando el intent configurado y el launcher registrado).
        */
        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    /*
    Autentica en Firebase usando las credenciales de Google, creando perfil si es nuevo usuario y navegando al dashboard.

    (Flujo completo: verifica credenciales → crea documento de usuario si no existe → redirige al dashboard).
    */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    /*
                    Verifica y crea el perfil del usuario en Firestore si no existe, luego muestra
                    mensaje de bienvenida y navega al dashboard.

                    (Gestiona el primer inicio de sesión guardando datos básicos del usuario antes de redirigir)
                     */
                    user?.let {
                        val uid = it.uid
                        val userRef = firestore.collection("users").document(uid)
                        userRef.get().addOnSuccessListener { document ->
                            if (!document.exists()) {
                                // Si no existe, crea documento con datos básicos
                                val newUser = mapOf(
                                    "nickname" to (user.displayName ?: ""),
                                    "email" to (user.email ?: "")
                                )
                                userRef.set(newUser)
                            }
                            Snackbar.make(binding.root, "Bienvenido ${user.displayName}", Snackbar.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                        }
                    }
                } else {
                    Snackbar.make(binding.root, "Error de autenticación con Google", Snackbar.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Error de signin con Google", Snackbar.LENGTH_SHORT).show()
            }
    }

    /*
    Autentica al usuario con email/contraseña en Firebase, muestra sus datos de perfil y navega al dashboard si es exitoso.
    (Flujo completo: verifica credenciales → recupera datos del usuario → muestra bienvenida personalizada → redirige).
     */
    private fun performLogin(email: String, password: String, view: View) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
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
                Snackbar.make(view, "Login fallido", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
