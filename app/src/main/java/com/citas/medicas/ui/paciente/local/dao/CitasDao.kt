package com.citas.medicas.ui.paciente.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.citas.medicas.ui.paciente.local.entities.CitaEntity

@Dao
interface CitasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCitas(citas: List<CitaEntity>)

    @Query("SELECT * FROM citas")
    suspend fun obtenerTodasLasCitas(): List<CitaEntity>

    @Query("DELETE FROM citas")
    suspend fun limpiarTablaCitas()
}