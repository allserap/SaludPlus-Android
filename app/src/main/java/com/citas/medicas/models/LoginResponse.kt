package com.citas.medicas.models
import com.google.gson.annotations.SerializedName

//objt a recibir
data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserProfile? = null,
)

//info real
data class UserProfile(
    @SerializedName("usuarioid") val id: String,
    val pacienteId: String?,
    val nombre: String,
    val apellido: String,
    val email: String,
    val telefono: String?,
    val dui: String?,
    @SerializedName("rolId") val rolId: Int,
    val numAfiliado: String?,
    val tipoSangre: String?,
    val alergias: String?,
    @SerializedName("condicionesCronicas") val condicionesCronicas: String?,
    val estadoFamiliar: String?,
    val token: String? = null
)