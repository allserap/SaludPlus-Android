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

fun com.citas.medicas.models.UnidadMedicaMapa.toEntity(): UnidadMedicaEntity {
    return UnidadMedicaEntity(
        id = this.id,
        nombre = this.nombre,
        direccion = this.direccion,
        telefono = this.telefono,
        latitud = this.latitud,
        longitud = this.longitud,
        especialidadesConcat = this.especialidades?.joinToString(",")
    )
}

fun UnidadMedicaEntity.toModel(): com.citas.medicas.models.UnidadMedicaMapa {
    return com.citas.medicas.models.UnidadMedicaMapa(
        id = this.id,
        nombre = this.nombre,
        direccion = this.direccion,
        telefono = this.telefono,
        latitud = this.latitud,
        longitud = this.longitud,
        especialidades = this.especialidadesConcat?.split(",")?.map { it.trim() } ?: emptyList()
    )
}
