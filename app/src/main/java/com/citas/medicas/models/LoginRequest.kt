package com.citas.medicas.models

import com.google.gson.annotations.SerializedName

//objt enviado
data class LoginRequest(
    val numAfiliado: String? = null,
    val numJvpm: String? = null,
    val email: String? = null,
    val password: String,
    //val rolId: Int
)

