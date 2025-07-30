package com.example.votchain

import android.content.Context

object AuthManager {
    private const val PREFS_NAME = "AuthPrefs"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_USER_ADDRESS = "userAddress"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_PRODI = "prodi"

    fun setLoggedIn(
        context: Context,
        isLoggedIn: Boolean,
        email: String? = null,
        address: String? = null,
        username: String? = null,
        prodi: String? = null
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            if (email != null) putString(KEY_USER_EMAIL, email)
            if (address != null) putString(KEY_USER_ADDRESS, address)
            if (username != null) putString(KEY_USER_NAME, username)
            if (prodi != null) putString(KEY_PRODI, prodi)
            apply()
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserEmail(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun getUserAddress(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_ADDRESS, null)
    }

    fun getUserName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun getProdi(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PRODI, null)
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}