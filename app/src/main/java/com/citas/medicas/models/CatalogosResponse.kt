package com.citas.medicas.models

import com.google.gson.annotations.SerializedName

//obtener catalogos
data class CatalogosResponse<T>(
    val success: Boolean,
    val data: T,
    val message: String
)

// --- Roles ---
data class RolResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String
)

// --- Especialidades ---
data class EspecialidadResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val activo: Boolean
)

// --- Unidades Médicas ---
data class UnidadMedicaResponse(
    @SerializedName("unidadmedicaid")
    val id: Int,
    @SerializedName("unidadmedica")
    val nombreCompleto: String
)