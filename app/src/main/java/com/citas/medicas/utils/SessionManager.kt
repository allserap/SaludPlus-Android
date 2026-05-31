package com.citas.medicas.utils

import android.content.Context
import android.content.Intent
import com.citas.medicas.ui.auth.LoginActivity

object SessionManager {
    fun logout(context: Context) {
        val prefs = context.getSharedPreferences("CitasMedicasPrefs", Context.MODE_PRIVATE)

        // guardar rol antes de limpiar
        val rol = prefs.getInt("user_id_rol", -1)

        prefs.edit().clear().apply()

        prefs.edit().putInt("user_id_rol", rol).apply()

        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra("rol", rol)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}