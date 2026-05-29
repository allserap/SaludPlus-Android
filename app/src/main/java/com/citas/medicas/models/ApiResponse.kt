package com.citas.medicas.models

import com.google.gson.annotations.SerializedName

//data class ApiResponseProximasCitas(
//    val exito: Boolean,
//    val datos: List<CitaHome>
//)
//
//data class CitaHome(
//    val id: String,
//    val especialidades: String,
//    val fecha_solicitada: String,
//    val hora_asignada: String,
//    val unidades_medicas: String,
//    val doctor: String
//)

// medico
data class MedicoProfileResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellido")
    val apellido: String,
    @SerializedName("dui")
    val dui: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("telefono")
    val telefono: String,
    @SerializedName("fechaNacimiento")
    val fechaNacimiento: String,
    @SerializedName("generoId")
    val generoId: String,
    @SerializedName("rolId")
    val rolId: Int,
    @SerializedName("activo")
    val activo: Boolean,
    @SerializedName("numJvmp")
    val numJvpm: String,
    @SerializedName("especialidad")
    val especialidad: Int,
    @SerializedName("unidadMedica")
    val unidadMedica: Int,
    val edad: Int,
    @SerializedName("genero")
    val genero: String
)

data class HistorialCitasResponse(
    val proximas: List<CitaFormateada>,
    val pasadas: List<CitaFormateada>
)

data class CitaFormateada(
    val id: Int,
    val estado: String, // "pendiente", "confirmada", "reprogramada", "completada", "cancelada"
    val especialidad_id: Int,
    val especialidad: String,
    val unidad_medica_id: Int,
    val fecha_solicitada: String,
    val hora_asignada: String,
    val unidad_medica: String,
    val doctor: String
)

data class UnidadEspecialidadRequest(
    @SerializedName("unidad_medica_id")
    val unidad_medica_id: Int,
    @SerializedName("especialidad_id")
    val especialidad_id: Int,
    @SerializedName("cupo_diario")
    val cupo_diario: Int,
    @SerializedName("activo")
    val activo: Boolean
)

data class UnidadEspecialidadResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("unidad_medica_id")
    val unidad_medica_id: Int,
    @SerializedName("especialidad_id")
    val especialidad_id: Int,
    @SerializedName("cupo_diario")
    val cupo_diario: Int,
    @SerializedName("activo")
    val activo: Boolean
)
data class ApiResponseHistorial(
    val exito: Boolean,
    val datos: DatosHistorial
)

data class DatosHistorial(
    val proximas: List<CitaHistorial>,
    val pasadas: List<CitaHistorial>
)

data class CitaHistorial(
    val id: String,
    val estado: String?,
    val especialidad: String?,
    val fecha_solicitada: String?,
    val hora_asignada: String?,
    val unidad_medica: String?,
    val doctor: String?,
    val especialidad_id: Int? = null,
    val unidad_medica_id: Int? = null
)

data class ApiResponsePerfil(
    val exito: Boolean,
    val datos: DatosPerfil
)

data class DatosPerfil(
    val nombre: String?,
    val apellido: String?,
    val dui: String?,
    val email: String?,
    val telefono: String?,
    val num_afiliado: String?,
    val tipo_sangre: String?,
    val alergias: String?,
    val condiciones_cronicas: String?,
    val medicinas: String?
)

data class ApiResponseMapa(
    val exito: Boolean,
    val datos: List<UnidadMedicaMapa>
)

data class UnidadMedicaMapa(
    val id: Int,
    val nombre: String?,
    val direccion: String?,
    val telefono: String?,
    val latitud: String?,
    val longitud: String?,
    val especialidades: List<String>?
)

data class ApiResponseEspecialidades(
    val success: Boolean,
    val data: List<Especialidad>,
    val message: String
)
data class Especialidad(
    val id: Int,
    val nombre: String?,
    val descripcion: String?
)

data class ApiResponseUnidades(
    val exito: Boolean,
    val datos: List<UnidadMedica>
)

data class UnidadMedica(
    val id: Int,
    val nombre: String,
    val direccion: String? = null
)
data class ApiResponseHorarios(
    val exito: Boolean,
    val datos: List<String>
)

data class CrearCitaRequest(
    val usuario_id: String,
    val especialidad_id: Int,
    val unidad_medica_id: Int,
    val fecha_solicitada: String,
    val hora_asignada: String,
    val motivo_consulta: String
)

data class ApiResponseCrearCita(
    val exito: Boolean,
    val mensaje: String
)

data class EditarPerfilRequest(
    val telefono: String,
    val alergias: String,
    val condiciones_cronicas: String
)

data class ActualizarCitaRequest(
    val estado_id: Int? = null,
    val fecha_solicitada: String? = null,
    val hora_asignada: String? = null
)

data class RespuestaGenerica(
    val exito: Boolean,
    val mensaje: String
)

data class ApiResponseEditarPerfil(
    val exito: Boolean,
    val mensaje: String
)

// acciones médico
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T?
)

data class AgendaCitasWrapper(
    @SerializedName("citas") val citas: List<CitaResponse>
)

data class MedicamentoWrapper(
    @SerializedName("medicamentos") val medicamentos: List<MedicamentoResponse>
)

data class CitaResponse(
    @SerializedName("pacienteid") val pacienteId: Int,
    @SerializedName("nombrepaciente") val nombrePaciente: String,
    @SerializedName("apellidopaciente") val apellidoPaciente: String,
    @SerializedName("medicoid") val medicoId: Int,
    @SerializedName("medicousuarioid") val medicoUsuarioId: String,
    @SerializedName("estadocita") val estadoCita: String,
    @SerializedName("especialidadid") val especialidadId: Int,
    @SerializedName("especialidadcita") val especialidadCita: String,
    @SerializedName("horaasignada") val horaAsignada: String
)

data class HistorialPacienteRequest(
    @SerializedName("tipoSangre") val tipoSangre: String?,
    @SerializedName("alergias") val alergias: String?,
    @SerializedName("condicionesCronicas") val condicionesCronicas: String?,
    @SerializedName("notaClinica") val notaClinica: String?,
    @SerializedName("medicamentosRecurrentes") val medicamentosRecurrentes: String?
)

data class HistorialPacienteResponse(
    @SerializedName("pacienteid") val pacienteId: Int,
    @SerializedName("usuarioId") val usuarioId: String,
    @SerializedName("estadoFamiliar") val estadoFamiliar: String,
    @SerializedName("numAfiliado") val numAfiliado: String,
    @SerializedName("tipoSangre") val tipoSangre: String,
    @SerializedName("alergias") val alergias: String,
    @SerializedName("condicionesCronicas") val condicionesCronicas: String,
    @SerializedName("notaClinica") val notaClinica: String,
    @SerializedName("medicamentosRecurrente") val medicamentosRecurrente: String
)

data class MedicamentoResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("nombreGenerico") val nombreGenerico: String,
    @SerializedName("nombreComercial") val nombreComercial: String,
    @SerializedName("formaFarmaceutica") val formaFarmaceutica: String,
    @SerializedName("concentracion") val concentracion: String,
    @SerializedName("activo") val activo: Boolean
)

data class AsistenciaRequest(
    @SerializedName("asistio") val asistio: Boolean
)

data class AsistenciaResponse(
    @SerializedName("asistenciacitaid") val asistenciaCitaId: Int,
    @SerializedName("citaid") val citaId: String,
    @SerializedName("medicoid") val medicoId: Int,
    @SerializedName("marcadoat") val marcadoAt: String,
    @SerializedName("asistio") val asistio: Boolean
)

data class RecetaRequest(
    @SerializedName("observaciones") val observaciones: String?
)

data class RecetaResponse(
    @SerializedName("recetaid") val recetaId: Int,
    @SerializedName("citaid") val citaId: String,
    @SerializedName("medicoid") val medicoId: Int,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("observaciones") val observaciones: String?
)

data class DetalleRecetaItemRequest(
    @SerializedName("recetaId") val recetaId: Int,
    @SerializedName("medicamentoId") val medicamentoId: Int,
    @SerializedName("dosis") val dosis: String,
    @SerializedName("duracionDias") val duracionDias: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("instrucciones") val instrucciones: String
)

data class DetalleRecetaResponse(
    @SerializedName("recetaid") val recetaId: Int,
    @SerializedName("medicamentoid") val medicamentoId: Int,
    @SerializedName("dosis") val dosis: String,
    @SerializedName("duraciondias") val duracionDias: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("instrucciones") val instrucciones: String
)