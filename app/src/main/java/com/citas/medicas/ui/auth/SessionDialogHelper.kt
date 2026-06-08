package com.citas.medicas.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.citas.medicas.data.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SessionDialogHelper {

    private var dialogAbierto = false

    fun mostrarDialogoExpiracion(context: Context) {
        // Evita que la app intente levantar un Dialog sobre una pantalla destruida o si ya hay uno abierto
        if (context !is Activity || context.isFinishing || dialogAbierto) return

        dialogAbierto = true

        AlertDialog.Builder(context)
            .setTitle("Sesión Expirada")
            .setMessage("Tu sesión ha caducado. ¿Deseas mantenerte conectado en SaludPlus?")
            .setCancelable(false) // Obligamos al usuario a tomar una decisión
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

    // refresh token
    private fun solicitarRefreshToken(context: Context) {
        val tokenActual = AuthManager.getToken(context) ?: return
        val requestBody = mapOf("token" to tokenActual)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.refreshToken(requestBody)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val nuevoToken = response.body()?.data?.token
                        if (nuevoToken != null) {
                            AuthManager.guardarNuevoToken(context, nuevoToken)
                            Toast.makeText(context, "Sesión renovada con éxito", Toast.LENGTH_SHORT).show()
                        } else {
                            ejecutarLogoutCompleto(context)
                        }
                    } else {
                        // El refresh token también caducó en base de datos
                        Toast.makeText(context, "Sesión inválida. Debe loguearse de nuevo", Toast.LENGTH_LONG).show()
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

     // Cierre de sesión definitivo (Invocado voluntariamente o por expiración rechazada)
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
                // aviso para borrar la fila de refresh_tokens
                apiService.logoutUsuario(requestBody)
            } catch (e: Exception) {
                // Si falla la red, ignoramos el error para no trabar el flujo del usuario
            } finally {
                withContext(Dispatchers.Main) {
                    // Limpieza local de SharedPreferences y redirección a LoginActivity
                    AuthManager.clearSessionLocal(context)
                }
            }
        }
    }
}