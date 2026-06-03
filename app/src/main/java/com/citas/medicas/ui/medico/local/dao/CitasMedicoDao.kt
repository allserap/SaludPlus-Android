package com.citas.medicas.ui.medico.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.citas.medicas.ui.medico.local.entities.CitaMedicoEntity

@Dao
interface CitasMedicoDao {

    @Query("SELECT * FROM agenda_medico")
    suspend fun obtenerTodasLasCitas(): List<CitaMedicoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarCitas(citas: List<CitaMedicoEntity>)

    @Query("DELETE FROM agenda_medico")
    suspend fun limpiarAgenda()
}