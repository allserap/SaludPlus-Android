package com.citas.medicas.ui.paciente.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. TABLA DE CITAS
@Entity(tableName = "citas")
data class CitaEntity(
    @PrimaryKey val id: String,
    val estado: String?,
    val especialidad: String?,
    val fecha_solicitada: String?,
    val hora_asignada: String?,
    val unidad_medica: String?,
    val doctor: String?,
    val especialidad_id: Int?,
    val unidad_medica_id: Int?
)

fun com.citas.medicas.models.CitaHistorial.toEntity(): CitaEntity {
    return CitaEntity(
        id = this.id,
        estado = this.estado,
        especialidad = this.especialidad,
        fecha_solicitada = this.fecha_solicitada,
        hora_asignada = this.hora_asignada,
        unidad_medica = this.unidad_medica,
        doctor = this.doctor,
        especialidad_id = this.especialidad_id,
        unidad_medica_id = this.unidad_medica_id
    )
}

fun CitaEntity.toModel(): com.citas.medicas.models.CitaHistorial {
    return com.citas.medicas.models.CitaHistorial(
        id = this.id,
        estado = this.estado,
        especialidad = this.especialidad,
        fecha_solicitada = this.fecha_solicitada,
        hora_asignada = this.hora_asignada,
        unidad_medica = this.unidad_medica,
        doctor = this.doctor,
        especialidad_id = this.especialidad_id,
        unidad_medica_id = this.unidad_medica_id
    )
}