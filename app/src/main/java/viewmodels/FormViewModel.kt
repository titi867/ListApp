package com.example.listapp.models

import androidx.lifecycle.ViewModel

class FormViewModel : ViewModel() {

    fun areFieldsValid(email: String, password: String): Boolean {
        return email.isNotBlank() && password.isNotBlank()
    }

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
