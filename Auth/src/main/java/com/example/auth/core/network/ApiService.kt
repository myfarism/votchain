package com.example.auth.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.*

// ApiService.kt
interface ApiService {
    @POST("register")
    suspend fun registerUser(@Body request: RegisterRequest): RegisterResponse

    @POST("verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): AuthResponse

    @POST("resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): ApiResponse

    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): AuthResponse

    @POST("logout")
    suspend fun logout(): ApiResponse

    @GET("user/{email}")
    suspend fun getUser(@Path("email") email: String): AuthResponse

    @GET("health")
    suspend fun healthCheck(): ApiResponse
}

// Request Data Classes
data class RegisterRequest(
    val email: String,
    val username: String,
    val prodi: String,
    val nim: String,
    val password: String,
    val confirmPassword: String,
    val address: String? = null,
    val signature: String? = null
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class ResendOtpRequest(
    val email: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

// Response Data Classes
data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val requiresVerification: Boolean = false
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: UserData? = null,
    val requiresVerification: Boolean = false
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val timestamp: String? = null
)

data class UserData(
    val email: String,
    val username: String,
    val nim: String,
    val prodi: String,
    val address: String
)

