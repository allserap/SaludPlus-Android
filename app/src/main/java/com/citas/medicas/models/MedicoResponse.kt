package com.citas.medicas.models

import com.google.gson.annotations.SerializedName

data class MedicoResponse (
    @SerializedName("id")
    val id: Int,
    @SerializedName("usuarioid")
    val usuarioId: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellido")
    val apellido: String,
    @SerializedName("fechanacimiento")
    val fechaNacimiento: String,
    val dui: String,
    val email: String,
    val telefono: String,
    val genero: String,
    @SerializedName("rolid")
    val rolId: Int,
    @SerializedName("activo")
    val activo: Boolean,
    @SerializedName("numjvpm")
    val numJvpm: String,
    @SerializedName("especialidadid")
    val especialidadId: Int,
    @SerializedName("unidadmedicaid")
    val unidadMedicaId: Int
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
    val message: String
)