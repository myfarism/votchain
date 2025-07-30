package com.example.auth.viewmodel

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.core.network.ApiClient
import com.example.auth.core.network.LoginRequest
import com.example.auth.core.network.RegisterRequest
import com.example.auth.core.network.ResendOtpRequest
import com.example.auth.core.network.UserData
import com.example.auth.core.network.VerifyOtpRequest
import com.example.auth.core.util.EthUtils
import com.example.auth.core.util.SecureStorage
import com.example.votchain.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// AuthViewModel.kt
class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser

    private val _pendingVerificationEmail = MutableStateFlow<String?>(null)
    val pendingVerificationEmail: StateFlow<String?> = _pendingVerificationEmail

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

                if (username.length < 3) {
                    _authState.value = AuthState.Error("Username minimal 3 karakter")
                    return@launch
                }

                if (nim.length < 8) {
                    _authState.value = AuthState.Error("NIM minimal 8 karakter")
                    return@launch
                }

                if (prodi.length < 2) {
                    _authState.value = AuthState.Error("Program studi harus diisi")
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
                    // Set pending verification email
                    _pendingVerificationEmail.value = email

                    // Store wallet data temporarily (will be saved permanently after verification)
                    SecureStorage.saveTemporaryWalletData(
                        context = context,
                        email = email,
                        address = wallet.address,
                        privateKey = wallet.privateKey
                    )

                    _authState.value = AuthState.RequiresVerification(response.message, email)
                } else {
                    _authState.value = AuthState.Error(response.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registrasi gagal")
            }
        }
    }

    fun verifyOtp(context: Context, email: String, otp: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = ApiClient.service.verifyOtp(
                    VerifyOtpRequest(
                        email = email,
                        otp = otp
                    )
                )

                if (response.success && response.user != null) {
                    // Set current user
                    _currentUser.value = response.user

                    // Move temporary wallet data to permanent storage
                    SecureStorage.moveTemporaryToPermanent(context, email)

                    // Save user data and token
                    SecureStorage.saveUserData(
                        context = context,
                        user = response.user,
                        token = response.token
                    )

                    // Clear pending verification
                    _pendingVerificationEmail.value = null

//                    _authState.value = AuthState.Success(response.message)
                    _authState.value = AuthState.Success(response.message ?: "Verify berhasil")
                } else {
                    _authState.value = AuthState.Error(response.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Verifikasi OTP gagal")
            }
        }
    }

    fun resendOtp(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = ApiClient.service.resendOtp(
                    ResendOtpRequest(email = email)
                )

                if (response.success) {
                    _authState.value = AuthState.Success(response.message)
                } else {
                    _authState.value = AuthState.Error(response.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Gagal mengirim ulang OTP")
            }
        }
    }

    fun login(context: Context, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                if (!isValidEmail(email)) {
                    _authState.value = AuthState.Error("Format email tidak valid")
                    return@launch
                }

                if (password.isEmpty()) {
                    _authState.value = AuthState.Error("Password tidak boleh kosong")
                    return@launch
                }

                Log.d("AuthViewModel", "Starting login for email: $email")

                val response = ApiClient.service.loginUser(
                    LoginRequest(
                        email = email,
                        password = password
                    )
                )

                Log.d("AuthViewModel", "Login response received: success=${response.success}")

                if (response.success && response.user != null) {
                    // Set current user
                    _currentUser.value = response.user
                    Log.d("AuthViewModel", "User set in currentUser state")

                    // Save user data and token to secure storage
                    try {
                        SecureStorage.saveUserData(
                            context = context,
                            user = response.user,
                            token = response.token
                        )
                        Log.d("AuthViewModel", "User data saved to secure storage")
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Error saving user data: ${e.message}")
                        // Don't fail login just because of storage error
                    }

                    // Check wallet data
                    val walletData = try {
                        SecureStorage.getUserWalletData(context, email)
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Error getting wallet data: ${e.message}")
                        null
                    }

                    Log.d("AuthViewModel", "Wallet data: $walletData")

                    if (walletData == null) {
                        Log.w("AuthViewModel", "No local wallet data found for user: $email")
                    }

                    // Set auth manager
                    try {
                        val address = walletData?.address ?: response.user.address ?: ""
                        val username = response.user.username ?: "User"
                        val prodi = response.user.prodi ?: "Prodi"
                        AuthManager.setLoggedIn(context, true, email, address, username, prodi)
                        Log.d("AuthViewModel", "AuthManager login status set")
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Error setting AuthManager: ${e.message}")
                        // Don't fail login just because of this
                    }

                    Log.d("AuthViewModel", "Login successful, setting success state")
                    _authState.value = AuthState.Success(response.message ?: "Login berhasil")

                } else if (response.requiresVerification == true) {
                    _pendingVerificationEmail.value = email
                    _authState.value = AuthState.RequiresVerification(
                        response.message ?: "Verifikasi diperlukan",
                        email
                    )
                } else {
                    val errorMessage = response.message ?: "Login gagal"
                    Log.w("AuthViewModel", "Login failed: $errorMessage")
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Login gagal - unknown error"
                Log.e("AuthViewModel", "Login exception: $errorMessage", e)
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            try {
                // Optional: panggil logout API
                try {
                    ApiClient.service.logout()
                } catch (e: Exception) {
                    // Ignore logout API error
                }

                // Clear local user data
                SecureStorage.clearUserData(context)

                // Clear login status
                AuthManager.logout(context)

                // Reset state
                _currentUser.value = null
                _pendingVerificationEmail.value = null
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Logout failed: ${e.message}")
            }
        }
    }


    fun checkUserSession(context: Context) {
        viewModelScope.launch {
            try {
                val userData = SecureStorage.getUserData(context)
                if (userData != null) {
                    // Verify with server
                    val response = ApiClient.service.getUser(userData.email)
                    if (response.success && response.user != null) {
                        _currentUser.value = response.user
                        _authState.value = AuthState.Success("Session restored")
                    } else {
                        // Session invalid, clear local data
                        SecureStorage.clearUserData(context)
                        _authState.value = AuthState.Idle
                    }
                } else {
                    _authState.value = AuthState.Idle
                }
            } catch (e: Exception) {
                // Network error or server down, use local data if available
                val userData = SecureStorage.getUserData(context)
                if (userData != null) {
                    _currentUser.value = userData
                    _authState.value = AuthState.Success("Using offline session")
                } else {
                    _authState.value = AuthState.Idle
                }
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
    data class Success(val message: String = "Operasi Berhasil") : AuthState()
    data class Error(val message: String = "Operasi terjadi error") : AuthState()
    data class RequiresVerification(val message: String, val email: String) : AuthState()
}