package com.citas.medicas.ui.medico.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.citas.medicas.models.CitaResponse

@Entity(tableName = "agenda_medico")
data class CitaMedicoEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val pacienteid: Int,
    val nombrepaciente: String,
    val apellidopaciente: String,
    val medicoid: Int,
    val medicousuarioid: String,
    val estadocita: String,
    val especialidadid: Int,
    val especialidadcita: String,
    val horaasignada: String,
    val fechacita: String
) {
    fun toModel(): CitaResponse {
        return CitaResponse(
            pacienteid = pacienteid,
            nombrepaciente = nombrepaciente,
            apellidopaciente = apellidopaciente,
            medicoid = medicoid,
            medicoUsuarioid = medicousuarioid,
            estadocita = estadocita,
            especialidadid = especialidadid,
            especialidadcita = especialidadcita,
            horaasignada = horaasignada,
            fechacita = fechacita
        )
    }

    companion object {
        fun fromModel(model: CitaResponse): CitaMedicoEntity {
            return CitaMedicoEntity(
                pacienteid = model.pacienteid,
                nombrepaciente = model.nombrepaciente,
                apellidopaciente = model.apellidopaciente,
                medicoid = model.medicoid,
                medicousuarioid = model.medicoUsuarioid,
                estadocita = model.estadocita,
                especialidadid = model.especialidadid,
                especialidadcita = model.especialidadcita,
                horaasignada = model.horaasignada,
                fechacita = model.fechacita
            )
        }
    }
}