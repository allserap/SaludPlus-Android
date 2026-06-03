package com.citas.medicas.data

import android.content.Context
import android.util.Log
import com.citas.medicas.models.AgendaCitasWrapper
import com.citas.medicas.models.ApiResponse
import com.citas.medicas.models.AsistenciaRequest
import com.citas.medicas.models.CatalogosResponse
import com.citas.medicas.models.DetalleRecetaItemRequest
import com.citas.medicas.models.DetalleRecetaResponse
import com.citas.medicas.models.HistoricoCitasResponse
import com.citas.medicas.models.MedicamentoWrapper
import com.citas.medicas.models.MedicoResponse
import com.citas.medicas.models.MedicoUpdateRequest
import com.citas.medicas.models.PacienteResponse
import com.citas.medicas.models.PacienteUpdateRequest
import com.citas.medicas.models.RecetaRequest
import com.citas.medicas.models.RecetaResponse
import com.citas.medicas.models.RegistroRequest
import com.citas.medicas.models.UnidadEspecialidadRequest
import com.citas.medicas.models.UnidadEspecialidadResponse
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

    suspend fun crearRecetaCabecera(pacienteId: Int, request: RecetaRequest): Response<ApiResponse<RecetaResponse>> {
        return apiService.crearRecetaCabecera(pacienteId, request)
    }

    suspend fun agregarMedicamentosAReceta(request: List<DetalleRecetaItemRequest>): Response<ApiResponse<List<DetalleRecetaResponse>>> {
        return apiService.agregarMedicamentosAReceta(request)
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

    suspend fun obtenerUnidadesEspecialidad(token: String? = null): Response<ApiResponse<List<UnidadEspecialidadResponse>>> {
    return apiService.obtenerUnidadesEspecialidad(token)}

    suspend fun marcarAsistenciaCita(citaUuid: String, request: AsistenciaRequest) =
        apiService.marcarAsistenciaCita(citaUuid, request)

    suspend fun obtenerHistoricoCitas(unidadMedicaId: Int?): Response<ApiResponse<List<HistoricoCitasResponse>>> {
        return apiService.obtenerHistoricoCitas(unidadMedicaId)
    }

    suspend fun obtenerTodasLasCitas(): Response<ApiResponse<AgendaCitasWrapper>> {
        return apiService.obtenerTodasLasCitas()
    }

    suspend fun obtenerTodosLosMedicamentos(): Response<ApiResponse<MedicamentoWrapper>> {
        return apiService.obtenerTodosLosMedicamentos()
    }
}