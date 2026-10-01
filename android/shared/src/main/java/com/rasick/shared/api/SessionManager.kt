package com.rasick.shared.api

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveSession(username: String, role: String, token: String? = null) {
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_ROLE, role)
            token?.let { putString(KEY_TOKEN, it) }
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_USERNAME)
            remove(KEY_ROLE)
            remove(KEY_TOKEN)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getUserRole(): String? {
        return prefs.getString(KEY_ROLE, null)
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    companion object {
        private const val PREF_NAME = "rasick_session_pref"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_TOKEN = "token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}
