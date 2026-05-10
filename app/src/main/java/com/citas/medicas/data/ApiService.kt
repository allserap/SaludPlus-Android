package com.citas.medicas.data

import com.citas.medicas.models.ApiResponse
import com.citas.medicas.models.ApiResponseCrearCita
import com.citas.medicas.models.ApiResponseEditarPerfil
import com.citas.medicas.models.ApiResponseEspecialidades
import com.citas.medicas.models.ApiResponseHistorial
import com.citas.medicas.models.ApiResponseHorarios
import com.citas.medicas.models.ApiResponseMapa
import com.citas.medicas.models.ApiResponsePerfil
import com.citas.medicas.models.ApiResponseProximasCitas
import com.citas.medicas.models.ApiResponseUnidades
import com.citas.medicas.models.CatalogosResponse
import com.citas.medicas.models.CrearCitaRequest
import com.citas.medicas.models.EditarPerfilRequest
import com.citas.medicas.models.EspecialidadResponse
import com.citas.medicas.models.LoginRequest
import com.citas.medicas.models.LoginResponse
import com.citas.medicas.models.MedicoResponse
import com.citas.medicas.models.MedicoUpdateRequest
import com.citas.medicas.models.RegistroRequest
import com.citas.medicas.models.RegistroResponse
import com.citas.medicas.models.RolResponse
import com.citas.medicas.models.UnidadMedicaResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    //auth
    @POST("auth/login")
    suspend fun loginUsuario(@Body request: LoginRequest): Response<LoginResponse>

    //Globales
    @GET("global/roles/catalogos")
    suspend fun obtenerRoles(): Response<CatalogosResponse<List<RolResponse>>>

    @GET("global/especialidades/catalogos")
    suspend fun obtenerEspecialidades(): Response<CatalogosResponse<List<EspecialidadResponse>>>

    @GET("global/unidades_medicas/catalogos")
    suspend fun obtenerUnidadesMedicas(): Response<CatalogosResponse<List<UnidadMedicaResponse>>>

    //Médicos (CRUD admin)
    @POST("admin/medicos/create")
    suspend fun registrarMedico(@Body request: RegistroRequest): Response<LoginResponse>

    @GET("admin/medicos/read")
    suspend fun obtenerMedicos(): Response<ApiResponse<List<MedicoResponse>>>

    @PUT("admin/medicos/update/{id}")
    suspend fun actualizarMedico(
        @Path("id") id: String,
        @Body medico: MedicoUpdateRequest
    ): Response<ResponseBody>

    @DELETE("admin/medicos/delete/{id}")
    suspend fun eliminarMedico(@Path("id") id: String): Response<Unit>

    //Pacientes
    @POST("auth/register/paciente")
    suspend fun registrarPaciente(@Body request: RegistroRequest): Response<RegistroResponse>

    @GET("paciente/proximas/{pacienteId}")
    suspend fun getProximasCitas(
        @Path("pacienteId") pacienteId: Int
    ): Response<ApiResponseProximasCitas>

    @GET("paciente/historial/{pacienteId}")
    suspend fun getHistorialCitas(
        @Path("pacienteId") pacienteId: Int
    ): Response<ApiResponseHistorial>

    @GET("paciente/perfil/{pacienteId}")
    suspend fun getPerfilPaciente(
        @Path("pacienteId") pacienteId: Int
    ): Response<ApiResponsePerfil>

    @GET("paciente/unidades-mapa")
    suspend fun getUnidadesMapa(): Response<ApiResponseMapa>

    // Paso 1 (Solicitar Cita)
    @GET("paciente/especialidades")
    suspend fun getEspecialidades(): Response<ApiResponseEspecialidades>

    // Paso 2 (Solicitar Cita)
    @GET("paciente/unidades-medicas/{idEspecialidad}")
    suspend fun getUnidadesFiltradas(
        @Path("idEspecialidad") idEspecialidad: Int
    ): Response<ApiResponseUnidades>

    // Paso 3 (Solicitar Cita)
    @GET("paciente/horarios-disponibles")
    suspend fun getHorariosDisponibles(
        @Query("unidadId") unidadId: Int,
        @Query("especialidadId") especialidadId: Int,
        @Query("fecha") fecha: String
    ): Response<ApiResponseHorarios>

    // POST para crear la cita
    @POST("paciente/agendar")
    suspend fun agendarCita(@Body request: CrearCitaRequest): Response<ApiResponseCrearCita>


    @PUT("paciente/perfil/{pacienteId}")
    suspend fun actualizarPerfilPaciente(
        @Path("pacienteId") pacienteId: Int,
        @Body request: EditarPerfilRequest
    ): Response<ApiResponseEditarPerfil>
}