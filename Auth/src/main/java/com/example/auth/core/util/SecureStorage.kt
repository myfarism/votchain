package com.example.auth.core.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.auth.core.network.UserData


// SecureStorage.kt
object SecureStorage {
    private const val PREFS_NAME = "secure_prefs"
    private const val TEMP_PREFS_NAME = "temp_secure_prefs"

    // Permanent storage keys
    private const val KEY_EMAIL = "email"
    private const val KEY_ADDRESS = "address"
    private const val KEY_PRIVATE_KEY = "private_key"
    private const val KEY_USERNAME = "username"
    private const val KEY_NIM = "nim"
    private const val KEY_PRODI = "prodi"
    private const val KEY_TOKEN = "jwt_token"

    // Temporary storage keys (for pending verification)
    private const val TEMP_KEY_EMAIL = "temp_email"
    private const val TEMP_KEY_ADDRESS = "temp_address"
    private const val TEMP_KEY_PRIVATE_KEY = "temp_private_key"

    private fun getEncryptedSharedPreferences(context: Context, prefsName: String = PREFS_NAME): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            prefsName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as SharedPreferences
    }

    // Save complete user data after successful verification
    fun saveUserData(
        context: Context,
        user: UserData,
        token: String? = null,
        address: String? = null,
        privateKey: String? = null
    ) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit()
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_NIM, user.nim)
            .putString(KEY_PRODI, user.prodi)
            .putString(KEY_ADDRESS, address ?: user.address)
            .apply {
                if (token != null) putString(KEY_TOKEN, token)
                if (privateKey != null) putString(KEY_PRIVATE_KEY, privateKey)
            }
            .apply()
    }

    // Save temporary wallet data during registration (before verification)
    fun saveTemporaryWalletData(
        context: Context,
        email: String,
        address: String,
        privateKey: String
    ) {
        val tempPrefs = getEncryptedSharedPreferences(context, TEMP_PREFS_NAME)
        tempPrefs.edit()
            .putString(TEMP_KEY_EMAIL, email)
            .putString(TEMP_KEY_ADDRESS, address)
            .putString(TEMP_KEY_PRIVATE_KEY, privateKey)
            .apply()
    }

    // Move temporary data to permanent storage after successful verification
    fun moveTemporaryToPermanent(context: Context, email: String) {
        val tempPrefs = getEncryptedSharedPreferences(context, TEMP_PREFS_NAME)
        val permanentPrefs = getEncryptedSharedPreferences(context)

        val tempAddress = tempPrefs.getString(TEMP_KEY_ADDRESS, null)
        val tempPrivateKey = tempPrefs.getString(TEMP_KEY_PRIVATE_KEY, null)

        if (tempAddress != null && tempPrivateKey != null) {
            // Move to permanent storage
            permanentPrefs.edit()
                .putString(KEY_ADDRESS, tempAddress)
                .putString(KEY_PRIVATE_KEY, tempPrivateKey)
                .apply()

            // Clear temporary storage
            clearTemporaryData(context)
        }
    }

    // Get complete user data
    fun getUserData(context: Context): UserData? {
        val prefs = getEncryptedSharedPreferences(context)
        val email = prefs.getString(KEY_EMAIL, null)
        val username = prefs.getString(KEY_USERNAME, null)
        val nim = prefs.getString(KEY_NIM, null)
        val prodi = prefs.getString(KEY_PRODI, null)
        val address = prefs.getString(KEY_ADDRESS, null)

        return if (email != null && username != null && nim != null && prodi != null && address != null) {
            UserData(
                email = email,
                username = username,
                nim = nim,
                prodi = prodi,
                address = address
            )
        } else {
            null
        }
    }

    // Get wallet data specifically
    fun getUserWalletData(context: Context, email: String): WalletData? {
        val prefs = getEncryptedSharedPreferences(context)
        val storedEmail = prefs.getString(KEY_EMAIL, null)
        val address = prefs.getString(KEY_ADDRESS, null)
        val privateKey = prefs.getString(KEY_PRIVATE_KEY, null)

        return if (storedEmail == email && address != null && privateKey != null) {
            WalletData(
                email = email,
                address = address,
                privateKey = privateKey
            )
        } else {
            null
        }
    }

    // Get stored JWT token
    fun getToken(context: Context): String? {
        val prefs = getEncryptedSharedPreferences(context)
        return prefs.getString(KEY_TOKEN, null)
    }

    // Save JWT token
    fun saveToken(context: Context, token: String) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    // Check if user data exists
    fun hasUserData(context: Context): Boolean {
        val prefs = getEncryptedSharedPreferences(context)
        return prefs.getString(KEY_EMAIL, null) != null
    }

    // Check if temporary data exists for email
    fun hasTemporaryData(context: Context, email: String): Boolean {
        val tempPrefs = getEncryptedSharedPreferences(context, TEMP_PREFS_NAME)
        val tempEmail = tempPrefs.getString(TEMP_KEY_EMAIL, null)
        return tempEmail == email
    }

    // Clear all user data
    fun clearUserData(context: Context) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit()
            .remove(KEY_EMAIL)
            .remove(KEY_USERNAME)
            .remove(KEY_NIM)
            .remove(KEY_PRODI)
            .remove(KEY_ADDRESS)
            .remove(KEY_PRIVATE_KEY)
            .remove(KEY_TOKEN)
            .apply()

        // Also clear temporary data
        clearTemporaryData(context)
    }

    // Clear temporary data
    fun clearTemporaryData(context: Context) {
        val tempPrefs = getEncryptedSharedPreferences(context, TEMP_PREFS_NAME)
        tempPrefs.edit()
            .remove(TEMP_KEY_EMAIL)
            .remove(TEMP_KEY_ADDRESS)
            .remove(TEMP_KEY_PRIVATE_KEY)
            .apply()
    }

    // Update user profile data
    fun updateUserProfile(context: Context, user: UserData) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit()
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_NIM, user.nim)
            .putString(KEY_PRODI, user.prodi)
            .putString(KEY_ADDRESS, user.address)
            .apply()
    }
}

// Data class for wallet information
data class WalletData(
    val email: String,
    val address: String,
    val privateKey: String
)