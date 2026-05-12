package com.citas.medicas.models

import com.google.gson.annotations.SerializedName

data class PacienteUpdateRequest (
    @SerializedName("id") val id: String, // ID del usuario (UUID)
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("dui") val dui: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String? = null,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String?,
    @SerializedName("genero") val genero: String,
    @SerializedName("rol") val rol: Int,
    @SerializedName("activo") val activo: Boolean,

    // Datos específicos de la tabla Pacientes
    @SerializedName("estadoFamiliar") val estadoFamiliar: String?,
    @SerializedName("numAfiliado") val numAfiliado: String?,
    @SerializedName("tipoSangre") val tipoSangre: String?,
    @SerializedName("alergias") val alergias: String?,
    @SerializedName("condicionesCronicas") val condicionesCronicas: String?,
    @SerializedName("notaClinica") val notaClinica: String?,
    @SerializedName("medicamentosRecurrentes") val medicamentosRecurrentes: String?
)