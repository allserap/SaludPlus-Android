package com.citas.medicas.models

data class AppointmentsResponse(
    val success: Boolean,
    val message: String,
    val data: AppointmentsData
)

data class AppointmentsData(
    val citas: List<CitaItem>
)

data class CitaItem(
    val pacienteid: Int,
    val nombrepaciente: String,
    val apellidopaciente: String,
    val medicoid: Int,
    val medicousuarioid: String,
    val estadocita: String,
    val especialidadid: Int,
    val especialidadcita: String,
    val horaasignada: String
)