package com.citas.medicas.models

import com.google.gson.annotations.SerializedName

data class PacienteResponse (
    @SerializedName("usuarioid")
    val usuarioId: String,
    val nombre: String,
    val apellido: String,
    val dui: String,
    val email: String,
    val telefono: String,
    @SerializedName("fechanacimiento")
    val fechaNacimiento: String?,
    val genero: String,
    @SerializedName("rolid")
    val rolId: Int,
    val activo: Boolean,
    @SerializedName("id")
    val pacienteId: Int,
    @SerializedName("estadofamiliar")
    val estadoFamiliar: String,
    @SerializedName("numafiliado")
    val numAfiliado: String?,
    @SerializedName("tiposangre")
    val tipoSangre: String?,
    val alergias: String?,
    @SerializedName("condicionescronicos")
    val condicionesCronicas: String?,
    @SerializedName("notaclinica")
    val notaClinica: String?,
    @SerializedName("medicamentosrecurrentes")
    val medicamentosRecurrentes: String?
)