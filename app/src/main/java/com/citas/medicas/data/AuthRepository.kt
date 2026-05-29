package com.citas.medicas.data

import android.content.Context
import android.util.Log
import com.citas.medicas.models.ApiResponse
import com.citas.medicas.models.CatalogosResponse
import com.citas.medicas.models.MedicoResponse
import com.citas.medicas.models.MedicoUpdateRequest
import com.citas.medicas.models.PacienteResponse
import com.citas.medicas.models.PacienteUpdateRequest
import com.citas.medicas.models.RegistroRequest
import com.citas.medicas.models.UnidadEspecialidadRequest
import com.citas.medicas.models.UnidadEspecialidadResponse
import kotlinx.coroutines.runBlocking
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
                Result.success("Actualización exitosa")
            } else {
                val errorServidor = response.errorBody()?.string()
                Log.e("API_DEBUG", "Error en transacción: $errorServidor")
                Result.failure(Exception(errorServidor ?: "Error al actualizar"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }

    suspend fun actualizarPaciente(datos: PacienteUpdateRequest): Result<String> {
        return try {
            val response = apiService.actualizarPaciente(datos.id, datos)
            if (response.isSuccessful) {
                Result.success("Actualización exitosa")
            } else {
                val errorServidor = response.errorBody()?.string()
                Log.e("API_DEBUG", "Error en transacción: $errorServidor")
                Result.failure(Exception(errorServidor ?: "Error al actualizar"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }

    // region NUEVO: Persistencia de datos para la combinación UnidadEspecialidad
    suspend fun buscarUnidadEspecialidad(unidadMedicaId: Int, especialidadId: Int): Response<ApiResponse<UnidadEspecialidadResponse>> {
        return apiService.buscarUnidadEspecialidad(unidadMedicaId, especialidadId)
    }

    /**
     * CREATE (POST): Inserta un nuevo registro de asignación médica con su cupo diario.
     */
    suspend fun crearUnidadEspecialidad(datos: UnidadEspecialidadRequest): Result<String> {
        return try {
            val response = apiService.crearUnidadEspecialidad(datos)
            if (response.isSuccessful) {
                Result.success("Asignación vinculada con éxito")
            } else {
                val errorServidor = response.errorBody()?.string()
                Log.e("API_DEBUG", "Error al crear UnidadEspecialidad: $errorServidor")
                Result.failure(Exception(errorServidor ?: "La combinación ya existe o los datos son inválidos"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }

    suspend fun actualizarUnidadEspecialidad(id: Int, datos: UnidadEspecialidadRequest): Result<String> {
        return try {
            val response = apiService.actualizarUnidadEspecialidad(id, datos)
            if (response.isSuccessful) {
                Result.success("Asignación modificada exitosamente")
            } else {
                val errorServidor = response.errorBody()?.string()
                Log.e("API_DEBUG", "Error al actualizar UnidadEspecialidad: $errorServidor")
                Result.failure(Exception(errorServidor ?: "Error al actualizar la asignación"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }
    // endregion

    suspend fun obtenerMedicos(token: String? = null): Response<CatalogosResponse<List<MedicoResponse>>> {
        return apiService.obtenerMedicos(token)
    }

    suspend fun obtenerPacientes(token: String? = null): Response<CatalogosResponse<List<PacienteResponse>>> {
        return apiService.obtenerPacientes(token)
    }

    suspend fun obtenerRoles() = apiService.obtenerRoles()

    suspend fun obtenerEspecialidades() = apiService.obtenerEspecialidades()

    suspend fun obtenerUnidadesMedicas() = apiService.obtenerUnidadesMedicas()
}