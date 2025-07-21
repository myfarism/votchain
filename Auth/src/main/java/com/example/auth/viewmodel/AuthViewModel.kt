package com.example.auth.viewmodel

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.core.network.ApiClient
import com.example.auth.core.network.LoginRequest
import com.example.auth.core.network.RegisterRequest
import com.example.auth.core.network.UserData
import com.example.auth.core.util.EthUtils
import com.example.auth.core.util.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// AuthViewModel.kt
class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser

    fun register(
        context: Context,
        email: String,
        username: String,
        prodi: String,
        nim: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Validasi input
                if (!isValidEmail(email)) {
                    _authState.value = AuthState.Error("Format email tidak valid")
                    return@launch
                }

                if (password != confirmPassword) {
                    _authState.value = AuthState.Error("Password tidak sama")
                    return@launch
                }

                if (password.length < 8) {
                    _authState.value = AuthState.Error("Password minimal 8 karakter")
                    return@launch
                }

                // Generate wallet baru
                val wallet = EthUtils.generateNewWallet()

                // Buat signature
                val message = "Register with email: $email, username: $username"
                val signature = EthUtils.signMessage(message, wallet.privateKey)

                // Kirim ke API
                val response = ApiClient.service.registerUser(
                    RegisterRequest(
                        email = email,
                        username = username,
                        prodi = prodi,
                        nim = nim,
                        password = password,
                        confirmPassword = confirmPassword,
                        address = wallet.address,
                        signature = signature
                    )
                )

                if (response.success) {
                    // Simpan data user
                    _currentUser.value = response.user
                    SecureStorage.saveUserData(
                        context = context,
                        email = email,
                        address = wallet.address,
                        privateKey = wallet.privateKey
                    )
                    _authState.value = AuthState.Success(response.message)
                } else {
                    _authState.value = AuthState.Error(response.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registrasi gagal")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = ApiClient.service.loginUser(
                    LoginRequest(
                        email = email,
                        password = password
                    )
                )

                if (response.success) {
                    _currentUser.value = response.user
                    _authState.value = AuthState.Success(response.message)
                } else {
                    _authState.value = AuthState.Error(response.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login gagal")
            }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            try {
                // Clear user data from secure storage
                SecureStorage.clearUserData(context)

                // Reset current user
                _currentUser.value = null
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Logout failed: ${e.message}")
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}