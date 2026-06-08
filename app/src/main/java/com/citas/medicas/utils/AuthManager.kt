package com.citas.medicas.utils

import android.content.Context
import android.content.Intent
import com.citas.medicas.ui.auth.LoginActivity

object AuthManager {
    private const val PREFS_NAME = "CitasMedicasPrefs"
    private const val KEY_TOKEN = "token_jwt"
    private const val KEY_ROL = "user_rolid"

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TOKEN, null)
    }

    fun guardarNuevoToken(context: Context, nuevoToken: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TOKEN, nuevoToken).apply()
    }

    // redirige al usuario al login
    fun clearSessionLocal(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}