package com.citas.medicas.ui.medico.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.citas.medicas.ui.medico.local.entities.MedicoPerfilEntity

@Dao
interface MedicoPerfilDao {
    @Query("SELECT * FROM perfil_medico LIMIT 1")
    suspend fun obtenerPerfil(): MedicoPerfilEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPerfil(perfil: MedicoPerfilEntity)

    @Query("DELETE FROM perfil_medico")
    suspend fun limpiarTabla()
}