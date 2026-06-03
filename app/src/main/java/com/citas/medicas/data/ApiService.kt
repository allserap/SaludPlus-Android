package com.citas.medicas.data

import com.citas.medicas.models.*
import okhttp3.Call
import com.citas.medicas.models.ActualizarCitaRequest
import com.citas.medicas.models.ApiResponse
import com.citas.medicas.models.ApiResponseCrearCita
import com.citas.medicas.models.ApiResponseEditarPerfil
import com.citas.medicas.models.ApiResponseEspecialidades
import com.citas.medicas.models.ApiResponseHistorial
import com.citas.medicas.models.ApiResponseHorarios
import com.citas.medicas.models.ApiResponseMapa
import com.citas.medicas.models.ApiResponsePerfil
//import com.citas.medicas.models.ApiResponseProximasCitas
import com.citas.medicas.models.ApiResponseUnidades
import com.citas.medicas.models.CatalogosResponse
import com.citas.medicas.models.CrearCitaRequest
import com.citas.medicas.models.EditarPerfilRequest
import com.citas.medicas.models.EspecialidadResponse
import com.citas.medicas.models.LoginRequest
import com.citas.medicas.models.LoginResponse
import com.citas.medicas.models.MedicoResponse
import com.citas.medicas.models.MedicoUpdateRequest
import com.citas.medicas.models.PacienteResponse
import com.citas.medicas.models.PacienteUpdateRequest
import com.citas.medicas.models.RegistroRequest
import com.citas.medicas.models.RegistroResponse
import com.citas.medicas.models.RespuestaGenerica
import com.citas.medicas.models.RolResponse
import com.citas.medicas.models.UnidadMedicaResponse
import com.google.android.gms.common.api.Api
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

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

    //-- Médicos --
    // (CRUD admin)
    @POST("admin/medicos/create")
    suspend fun registrarMedico(@Body request: RegistroRequest): Response<LoginResponse>

    @GET("admin/medicos/read")
    // suspend fun obtenerMedicos(): Response<ApiResponse<List<MedicoResponse>>>
    suspend fun obtenerMedicos(
        @Header("Authorization") token: String? = null
    ): Response<CatalogosResponse<List<MedicoResponse>>>

    @PUT("admin/medicos/update/{id}")
    suspend fun actualizarMedico(
        @Path("id") id: String,
        @Body medico: MedicoUpdateRequest
    ): Response<ResponseBody>

    @DELETE("admin/medicos/delete/{id}")
    suspend fun eliminarMedico(@Path("id") id: Int): Response<Unit>

    @GET("admin/unidad_especialidad/read")
    suspend fun obtenerUnidadesEspecialidad(
        @Header("Authorization") token: String? = null
    ): Response<ApiResponse<List<UnidadEspecialidadResponse>>>

    /*@POST("admin/unidad_especialidad/create")
    suspend fun crearUnidadEspecialidad(
        @Body request: UnidadEspecialidadRequest
    ): Response<UnidadEspecialidadResponse>*/

    @PUT("admin/unidad_especialidad/update/{id}")
    suspend fun actualizarUnidadEspecialidad(
        @Path("id") id: Int,
        @Body request: UnidadEspecialidadRequest
    ): Response<UnidadEspecialidadResponse>

    // funciones de médico
    @GET("medico/profile")
    suspend fun obtenerPerfilMedicoLogueado(): Response<ApiResponse<MedicoProfileResponse>>

    //Pacientes
    @POST("auth/register/paciente")
    suspend fun registrarPaciente(@Body request: RegistroRequest): Response<RegistroResponse>

    // Obtener todos los pacientes
    @GET("admin/pacientes/read")
    suspend fun obtenerPacientes(
        @Header("Authorization") token: String? = null
    ): Response<CatalogosResponse<List<PacienteResponse>>>

    // Obtener histórico
    @GET("admin/estadisticas/historico_citas")
    suspend fun obtenerHistoricoCitas(
        @Query("id") unidadMedicaId: Int?
    ): Response<ApiResponse<List<HistoricoCitasResponse>>>

    // Actualizar paciente por ID
    @POST("admin/pacientes/update/{id}")
    suspend fun actualizarPaciente(
        @Path("id") id: String,
        @Body request: PacienteUpdateRequest
    ): Response<PacienteResponse>

    // Eliminación lógica (Desactivar)
    @DELETE("admin/pacientes/delete/{id}")
    suspend fun desactivarPaciente(
        @Path("id") id: String
    ): Response<PacienteResponse>

//    @GET("paciente/proximas/{usuarioId}")
//    suspend fun getProximasCitas(
//        @Path("usuarioId") usuarioId: String
//    ): Response<ApiResponseProximasCitas>

    @GET("paciente/historial/{usuarioId}")
    suspend fun getHistorialCitas(
        @Path("usuarioId") usuarioId: String
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
//    @GET("paciente/unidades-medicas/{idEspecialidad}")
//    suspend fun getUnidadesFiltradas(
//        @Path("idEspecialidad") idEspecialidad: Int
//    ): Response<ApiResponseUnidades>

    @GET("global/unidades-medicas/{idEspecialidad}")
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


    //reprogrmar cita
    @PATCH("paciente/actualizar-cita/{citaId}")
    suspend fun actualizarCita(
        @Path("citaId") citaId: String,
        @Body request: ActualizarCitaRequest
    ): Response<RespuestaGenerica>

    @PUT("paciente/perfil/{pacienteId}")
    suspend fun actualizarPerfilPaciente(
        @Path("pacienteId") pacienteId: Int,
        @Body request: EditarPerfilRequest
    ): Response<ApiResponseEditarPerfil>

    // =========================================================================
    // AGREGADO: NUEVOS ENDPOINTS PARA EL FLUJO OPERATIVO DEL MÉDICO
    // =========================================================================

    @GET("medico/citas/allAppointments")
    suspend fun obtenerTodasLasCitas(): Response<ApiResponse<AgendaCitasWrapper>>

/*    @GET("medico/historialPaciente/information/{id}")
    suspend fun obtenerInformacionPaciente(
        @Path("id") pacienteId: Int
    ): Response<ApiResponse<PacienteResponse>>

    @PATCH("medico/paciente/historial/update/{id}")
    suspend fun actualizarHistorialPaciente(
        @Path("id") pacienteId: Int,
        @Body request: HistorialPacienteRequest
    ): Response<ApiResponse<HistorialPacienteResponse>>*/

    @GET("medico/medicina")
    suspend fun obtenerTodosLosMedicamentos(): Response<ApiResponse<MedicamentoWrapper>>

    @POST("medico/asistencia/update/{id}/")
    suspend fun marcarAsistenciaCita(
        @Path("id") citaUuid: String,
        @Body request: AsistenciaRequest
    ): Response<ApiResponse<AsistenciaResponse>>

    @POST("medico/receta/create/{pacienteId}")
    suspend fun crearRecetaCabecera(
        @Path("pacienteId") pacienteId: Int,
        @Body request: RecetaRequest
    ): Response<ApiResponse<RecetaResponse>>

    @POST("medico/receta/medicamento/create")
    suspend fun agregarMedicamentosAReceta(
        @Body request: List<DetalleRecetaItemRequest>
    ): Response<ApiResponse<List<DetalleRecetaResponse>>>
}