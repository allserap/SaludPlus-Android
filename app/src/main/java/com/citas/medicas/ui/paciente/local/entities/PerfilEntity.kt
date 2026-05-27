package com.citas.medicas.ui.paciente.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfil_usuario")
data class PerfilEntity(
    @PrimaryKey val localId: Int = 1,
    val nombre: String?,
    val apellido: String?,
    val dui: String?,
    val email: String?,
    val telefono: String?,
    val num_afiliado: String?,
    val tipo_sangre: String?,
    val alergias: String?,
    val condiciones_cronicas: String?,
    val medicinas: String?
)