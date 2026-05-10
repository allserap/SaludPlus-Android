package com.citas.medicas.utils

import com.citas.medicas.models.RolResponse
import kotlin.collections.find

object RolesUsuario {
    // Variables para almacenar los IDs dinámicos
    var ID_PACIENTE: Int = 1
    var ID_MEDICO: Int = 2
    var ID_ADMIN: Int = 3

    fun inicializar(listaRoles: List<RolResponse>) {
        ID_PACIENTE = listaRoles.find { it.nombre.equals("Paciente", ignoreCase = true) }?.id ?: 1
        ID_MEDICO = listaRoles.find { it.nombre.equals("Medico", ignoreCase = true) }?.id ?: 2
        ID_ADMIN = listaRoles.find { it.nombre.equals("Administrador", ignoreCase = true) ||
                it.nombre.equals("Admin", ignoreCase = true) }?.id ?: 3
    }
}