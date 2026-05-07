package com.citas.medicas.models

data class MedicoUpdateRequest (
    val id: String,
    val nombre: String,
    val apellido: String,
    val dui: String,
    val email: String,
    val telefono: String,
    val fechaNacimiento: String,
    val genero: Int,
    val rol: Int,
    val activo: Boolean,
    val numJvpm: String,
    val especialidad: Int,
    val unidadMedica: Int,
    val password: String? = null
)