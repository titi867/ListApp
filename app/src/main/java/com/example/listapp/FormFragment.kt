package com.example.listapp

import com.example.listapp.models.FormViewModel
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.listapp.databinding.FragmentFormBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class FormFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentFormBinding
    private val calendar = Calendar.getInstance()
    private val formViewModel: FormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
        binding = FragmentFormBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext())
            .load("https://goo.gl/gEgYUd")
            .into(binding.ivPhoto)

        binding.etBirthday.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    binding.etBirthday.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnFormSignUp.setOnClickListener {
            val email = binding.etFormEmail.text.toString().trim()
            val password = binding.etFormPassword.text.toString().trim()
            val nickname = binding.etNickname.text.toString().trim()
            val birthDate = binding.etBirthday.text.toString().trim()

            if (!formViewModel.areFieldsValid(email, password)) {
                Snackbar.make(it, getString(R.string.error_empty_fields), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!formViewModel.isEmailValid(email)) {
                Snackbar.make(it, getString(R.string.error_invalid_email), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val userMap = hashMapOf(
                        "email" to email,
                        "nickname" to nickname,
                        "birthday" to birthDate
                    )

                    userId?.let {
                        firestore.collection("users").document(it).set(userMap)
                            .addOnSuccessListener {
                                Snackbar.make(view, getString(R.string.success_user_created), Snackbar.LENGTH_SHORT).show()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    findNavController().navigate(R.id.action_formFragment_to_loginFragment)
                                }, 2000)
                            }
                            .addOnFailureListener {
                                Snackbar.make(view, getString(R.string.error_saving_user), Snackbar.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Snackbar.make(view, getString(R.string.error_creating_user), Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnFormLogIn.setOnClickListener {
            findNavController().navigate(R.id.action_formFragment_to_loginFragment)
        }
    }
}
