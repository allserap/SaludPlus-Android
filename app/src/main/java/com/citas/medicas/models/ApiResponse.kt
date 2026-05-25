package com.citas.medicas.models

import com.google.gson.annotations.SerializedName

data class ApiResponseProximasCitas(
    val exito: Boolean,
    val datos: List<CitaHome>
)

data class CitaHome(
    val id: String,
    val especialidades: String,
    val fecha_solicitada: String,
    val hora_asignada: String,
    val unidades_medicas: String,
    val doctor: String
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