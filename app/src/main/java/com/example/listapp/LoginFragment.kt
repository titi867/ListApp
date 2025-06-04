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

        // Configura opciones de Google Sign-In
            //. Comando para generar la llave/gradlew signingReport
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("023194829429-l8igpnhqccomtusnl59r1iv31n1vo5qn.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // Inicializa el launcher moderno de resultado de actividad
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Snackbar.make(binding.root, "Fallo Google Sign-In: ${e.message}", Snackbar.LENGTH_SHORT).show()
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

        // Google Sign-In
        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
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
    }

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
