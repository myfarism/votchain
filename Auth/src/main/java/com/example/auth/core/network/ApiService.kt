package com.example.auth.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.*

interface ApiService {
    @POST("register")
    suspend fun registerUser(@Body request: RegisterRequest): AuthResponse

    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): AuthResponse

    @GET("user/{email}")
    suspend fun getUser(@Path("email") email: String): AuthResponse
}

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

data class LoginRequest(
    val email: String,
    val password: String
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val user: UserResponse? = null
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: UserData? = null
)

data class UserResponse(
    val address: String,
    val username: String
)

data class UserData(
    val address: String,
    val email: String,
    val username: String,
    val prodi: String,
    val nim: String
)