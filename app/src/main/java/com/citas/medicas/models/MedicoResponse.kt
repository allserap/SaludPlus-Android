package com.citas.medicas.models

import com.google.gson.annotations.SerializedName

data class MedicoResponse (
    val usuarioId: String,
    val nombre: String,
    val apellido: String,
    val dui: String,
    val email: String,
    val telefono: String,
    val fechaNacimiento: String,
    val genero: String,
    val activo: Boolean,
    val numJvpm: String,
    @SerializedName("rolid")
    val rolId: Int,
    @SerializedName("especialidadid")
    val especialidadId: Int,
    @SerializedName("especialidad")
    val especialidad: String,
    @SerializedName("unidadmedicaid")
    val unidadMedicaId: Int,
    @SerializedName("unidadmedica")
    val unidadMedica: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
    val message: String
)