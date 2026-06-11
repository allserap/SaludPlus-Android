package com.citas.medicas.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import com.citas.medicas.data.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SessionDialogHelper {

    private var dialogAbierto = false

    fun mostrarDialogoExpiracion(context: Context) {
        if (context !is Activity || context.isFinishing || context.isDestroyed || dialogAbierto) return

        dialogAbierto = true

        AlertDialog.Builder(context)
            .setTitle("Sesión expirada")
            .setMessage("Tu sesión ha caducado. Por favor, inicia sesión nuevamente.")
            .setCancelable(false)
            .setPositiveButton("Iniciar sesión") { dialog, _ ->
                dialog.dismiss()
                dialogAbierto = false
                ejecutarLogoutCompleto(context)
            }
            .show()
    }

    fun ejecutarLogoutCompleto(context: Context) {
        val tokenActual = AuthManager.getToken(context)

        if (tokenActual == null) {
            AuthManager.clearSessionLocal(context)
            return
        }

        val requestBody = mapOf("token" to tokenActual)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.getApiService(context)
                apiService.logoutUsuario(requestBody)
            } catch (e: Exception) {
                // Error de red en logout: lo ignoramos para no trabar el flujo
            } finally {
                withContext(Dispatchers.Main) {
                    AuthManager.clearSessionLocal(context)
                }
            }
        }
    }
}