package com.example.listapp.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    // LiveData que notifica si el formulario de login es válido
    private val _loginFormValid = MutableLiveData<Boolean>()
    val loginFormValid: LiveData<Boolean> get() = _loginFormValid

    /**
     * Verifica que los campos de email y contraseña no estén vacíos.
     * Publica true en loginFormValid si ambos están completos, false si no.
     */
    fun validateCredentials(email: String, password: String) {
        _loginFormValid.value = email.isNotBlank() && password.isNotBlank()
    }
}