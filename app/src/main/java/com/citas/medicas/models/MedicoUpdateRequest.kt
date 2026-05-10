package com.citas.medicas.models

import com.google.gson.annotations.SerializedName

data class MedicoUpdateRequest (
    val id: String,
    val nombre: String,
    val apellido: String,
    val dui: String,
    val email: String,
    val telefono: String,
    @SerializedName("fechaNacimiento")
    val fechaNacimiento: String?,
    val genero: String,
    @SerializedName("rol")
    val rol: Int,
    @SerializedName("activo")
    val activo: Boolean,
    val numJvpm: String,
    val especialidad: Int,
    val unidadMedica: Int,
    val password: String?
)