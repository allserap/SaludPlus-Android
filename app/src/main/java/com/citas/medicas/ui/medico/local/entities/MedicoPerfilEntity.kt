package com.citas.medicas.ui.medico.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.citas.medicas.models.MedicoProfileResponse

@Entity(tableName = "perfil_medico")
data class MedicoPerfilEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val apellido: String,
    val dui: String,
    val email: String,
    val telefono: String,
    val fechaNacimiento: String,
    val genero: String,
    val generoId: String,
    val rolId: Int,
    val activo: Boolean,
    val numJvpm: String,
    val especialidad: Int,
    val unidadMedica: Int,
    val edad: Int
) {

    fun toModel(): MedicoProfileResponse {
        return MedicoProfileResponse(
            id = id, nombre = nombre, apellido = apellido, dui = dui,
            email = email, telefono = telefono, fechaNacimiento = fechaNacimiento,
            genero = genero, generoId = generoId, rolId = rolId,
            activo = activo, numJvpm = numJvpm, especialidad = especialidad,
            unidadMedica = unidadMedica,
            edad = edad
        )
    }

    companion object {
        fun fromModel(model: MedicoProfileResponse): MedicoPerfilEntity {
            return MedicoPerfilEntity(
                id = model.id, nombre = model.nombre, apellido = model.apellido, dui = model.dui,
                email = model.email, telefono = model.telefono, fechaNacimiento = model.fechaNacimiento,
                genero = model.genero, generoId = model.generoId, rolId = model.rolId,
                activo = model.activo, numJvpm = model.numJvpm, especialidad = model.especialidad,
                unidadMedica = model.unidadMedica,
                edad = model.edad
            )
        }
    }
}