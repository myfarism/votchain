package com.example.auth.core.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.auth.core.network.UserData


object SecureStorage {
    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_EMAIL = "email"
    private const val KEY_ADDRESS = "address"
    private const val KEY_PRIVATE_KEY = "private_key"

    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as SharedPreferences
    }

    fun saveUserData(context: Context, email: String, address: String, privateKey: String) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_ADDRESS, address)
            .putString(KEY_PRIVATE_KEY, privateKey)
            .apply()
    }

    fun getUserData(context: Context): UserData? {
        val prefs = getEncryptedSharedPreferences(context)
        val email = prefs.getString(KEY_EMAIL, null)
        val address = prefs.getString(KEY_ADDRESS, null)
        val privateKey = prefs.getString(KEY_PRIVATE_KEY, null)

        return if (email != null && address != null && privateKey != null) {
            UserData(address, email, "", "", "") // Data lain bisa diambil dari API
        } else {
            null
        }
    }

    fun clearUserData(context: Context) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit()
            .remove(KEY_EMAIL)
            .remove(KEY_ADDRESS)
            .remove(KEY_PRIVATE_KEY)
            .apply()
    }
}