package com.citas.medicas.data

import android.content.Context
import android.util.Log
import com.citas.medicas.models.ApiResponse
import com.citas.medicas.models.MedicoResponse
import com.citas.medicas.models.MedicoUpdateRequest
import com.citas.medicas.models.RegistroRequest
import retrofit2.Response

class AuthRepository(private val context: Context) {
    private val apiService = RetrofitClient.getApiService(context)
    suspend fun registrarMedico(request: RegistroRequest): Result<String?> {
        return try {
            val response = apiService.registrarMedico(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message)
            } else {
                val errorServidor = response.errorBody()?.string()
                Log.e("API_DEBUG", "Error en transacción: $errorServidor")
                Result.failure(Exception(errorServidor ?: "Error en el registro"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }
    suspend fun registrarPaciente(request: RegistroRequest): Result<String?> {
        return try {
            val response = apiService.registrarPaciente(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message)
            } else {
                val errorServidor = response.errorBody()?.string()
                Log.e("API_DEBUG", "Error en transacción: $errorServidor")
                Result.failure(Exception(errorServidor ?: "Error en el registro"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }

    suspend fun actualizarMedico(datos: MedicoUpdateRequest): Result<String> {
        return try {
            val response = apiService.actualizarMedico(datos.id, datos)
            if (response.isSuccessful) {
                Result.success("Médico actualizado exitosamente")
            } else {
                val errorServidor = response.errorBody()?.string()
                Log.e("API_DEBUG", "Error en transacción: $errorServidor")
                Result.failure(Exception(errorServidor ?: "Error al actualizar"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }

    suspend fun obtenerMedicos(): Response<ApiResponse<List<MedicoResponse>>> {
        return apiService.obtenerMedicos()
    }
    suspend fun obtenerRoles() = apiService.obtenerRoles()

    suspend fun obtenerEspecialidades() = apiService.obtenerEspecialidades()

    suspend fun obtenerUnidadesMedicas() = apiService.obtenerUnidadesMedicas()
}