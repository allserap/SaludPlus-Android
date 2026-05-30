package com.citas.medicas.ui.paciente.local.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.citas.medicas.ui.paciente.local.entities.UnidadMedicaEntity

@Dao
interface UnidadMedicaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUnidades(unidades: List<UnidadMedicaEntity>)

    @Query("SELECT * FROM unidades_medicas")
    suspend fun obtenerTodasLasUnidades(): List<UnidadMedicaEntity>

    @Query("DELETE FROM unidades_medicas")
    suspend fun limpiarTablaUnidades()

}