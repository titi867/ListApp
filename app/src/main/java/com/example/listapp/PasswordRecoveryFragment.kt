package com.example.listapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.activity.addCallback
import com.example.listapp.databinding.FragmentPasswordRecoveryBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

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

        val view = inflater.inflate(R.layout.fragment_password_recovery, container, false)

        val btnResetPassword = view.findViewById<Button>(R.id.btnResetPassword)
        val etRecoveryEmail = view.findViewById<TextInputEditText>(R.id.etRecoveryEmail)
        val etRecoveryPassword = view.findViewById<TextInputEditText>(R.id.etRecoveryPassword)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val textInputLayoutRecoveryEmail = view.findViewById<TextInputLayout>(R.id.textInputLayoutRecoveryEmail)
        val textInputLayoutRecoveryPassword = view.findViewById<TextInputLayout>(R.id.textInputLayoutRecoveryPassword)
        val textInputLayoutConfirmPassword = view.findViewById<TextInputLayout>(R.id.textInputLayoutConfirmPassword)
        var canResetPassword = true
        val email = etRecoveryEmail.text.toString().trim()

        btnResetPassword.isEnabled = true

        btnResetPassword.setOnClickListener {

            textInputLayoutRecoveryEmail.error = ""
            textInputLayoutRecoveryPassword.error = ""
            textInputLayoutConfirmPassword.error = ""

            var canResetPassword = true

            if (!isValidEmail(etRecoveryEmail.text.toString())) {
                textInputLayoutRecoveryEmail.error = getString(R.string.write_valid_email)
                canResetPassword = false
            }

            val recoveryPassword = etRecoveryPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (recoveryPassword.length < 8) {
                textInputLayoutRecoveryPassword.error = getString(R.string.password_8_char)
                canResetPassword = false
            }

            if (confirmPassword != recoveryPassword) {
                textInputLayoutConfirmPassword.error = getString(R.string.password_must_be_the_same)
                canResetPassword = false
            }

            if (canResetPassword){
                Snackbar.make(it, getString(R.string.password_is_reset), Snackbar.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    findNavController().navigate(R.id.action_passwordRecoveryFragment_to_loginFragment)
                }, 2000)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding = FragmentPasswordRecoveryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etRecoveryEmail.text.toString().trim()
            if (!isValidEmail(email)) {
                Snackbar.make(view, "Enter a valid email", Snackbar.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Snackbar.make(view, "A password recovery mail has been sent", Snackbar.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            findNavController().navigate(R.id.action_passwordRecoveryFragment_to_loginFragment)
                        }, 2000)
                    } else {
                        Snackbar.make(view, "Error while sending the password recovery mail", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

        fun isValidEmail(email:String): Boolean{
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()

    }


}