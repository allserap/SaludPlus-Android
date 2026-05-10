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

    @SerializedName("usuarioId") val id: String,

    val nombre: String,
    val apellido: String,
    val email: String,
    @SerializedName("rolid")
    val rolId: Int,
    // Datos adicionales del SELECT de Node.js
    @SerializedName("numafiliado")
    val numAfiliado: String? = null,
    @SerializedName("numjvpm")
    val numJvpm: String? = null,
    @SerializedName("especialidadid")
    val especialidadId: Int? = null,
    val token: String? = null
)