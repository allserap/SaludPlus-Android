package com.citas.medicas.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
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
            .setTitle("Sesión Expirada")
            .setMessage("Tu sesión ha caducado. ¿Deseas mantenerte conectado en SaludPlus?")
            .setCancelable(false)
            .setPositiveButton("Mantener sesión activa") { dialog, _ ->
                dialogAbierto = false
                dialog.dismiss()
                solicitarRefreshToken(context)
            }
            .setNegativeButton("Cerrar Sesión") { dialog, _ ->
                dialogAbierto = false
                dialog.dismiss()
                ejecutarLogoutCompleto(context)
            }
            .show()
    }

    private fun solicitarRefreshToken(context: Context) {
        val tokenActual = AuthManager.getToken(context)

        if (tokenActual.isNullOrEmpty()) {
            ejecutarLogoutCompleto(context)
            return
        }

        // Estructura idéntica a req.body esperada por el endpoint separado
        val requestBody = mapOf("refreshToken" to tokenActual)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.refreshToken(requestBody)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val jsonResponse = response.body()!!

                        if (jsonResponse.get("success").asBoolean) {
                            val dataObj = jsonResponse.getAsJsonObject("data")
                            val nuevoToken = dataObj?.get("accessToken")?.asString

                            if (!nuevoToken.isNullOrEmpty()) {
                                AuthManager.guardarNuevoToken(context, nuevoToken)
                                Toast.makeText(context, "Sesión renovada con éxito", Toast.LENGTH_SHORT).show()
                            } else {
                                ejecutarLogoutCompleto(context)
                            }
                        } else {
                            ejecutarLogoutCompleto(context)
                        }
                    } else {
                        ejecutarLogoutCompleto(context)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error de red al renovar sesión", Toast.LENGTH_SHORT).show()
                    ejecutarLogoutCompleto(context)
                }
            }
        }
    }

    fun ejecutarLogoutCompleto(context: Context) {
        val tokenActual = AuthManager.getToken(context)

        if (tokenActual.isNullOrEmpty()) {
            AuthManager.clearSessionLocal(context)
            return
        }

        val requestBody = mapOf("token" to tokenActual)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.getApiService(context)
                apiService.logoutUsuario(requestBody)
            } catch (e: Exception) {
                Log.e("LOGOUT_ERROR", "Error silenciado en red de logout", e)
            } finally {
                withContext(Dispatchers.Main) {
                    AuthManager.clearSessionLocal(context)
                }
            }
        }
    }
}