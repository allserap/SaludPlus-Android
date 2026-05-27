package com.citas.medicas.ui.paciente.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unidades_medicas")
data class UnidadMedicaEntity(
    @PrimaryKey val id: Int,
    val nombre: String?,
    val direccion: String?,
    val telefono: String?,
    val latitud: String?,
    val longitud: String?,
    val especialidadesConcat: String?
)