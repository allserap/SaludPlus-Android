package com.citas.medicas.ui.paciente.local.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.citas.medicas.ui.paciente.local.entities.PerfilEntity

@Dao
interface PerfilDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPerfil(perfil: PerfilEntity)

    @Query("SELECT * FROM perfil_usuario WHERE localId = 1 LIMIT 1")
    suspend fun obtenerPerfil(): PerfilEntity?
}