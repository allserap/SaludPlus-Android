package com.citas.medicas.ui

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.citas.medicas.ui.medico.local.dao.CitasMedicoDao
import com.citas.medicas.ui.medico.local.dao.MedicoPerfilDao
import com.citas.medicas.ui.medico.local.entities.CitaMedicoEntity
import com.citas.medicas.ui.medico.local.entities.MedicoPerfilEntity
import com.citas.medicas.ui.paciente.local.dao.CitasDao
import com.citas.medicas.ui.paciente.local.dao.PerfilDao
import com.citas.medicas.ui.paciente.local.dao.UnidadMedicaDao
import com.citas.medicas.ui.paciente.local.entities.CitaEntity
import com.citas.medicas.ui.paciente.local.entities.PerfilEntity
import com.citas.medicas.ui.paciente.local.entities.UnidadMedicaEntity

@Database(
    entities = [
        CitaEntity::class,
        PerfilEntity::class,
        UnidadMedicaEntity::class,
        MedicoPerfilEntity::class,
        CitaMedicoEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs del Paciente
    abstract fun citasDao(): CitasDao
    abstract fun perfilDao(): PerfilDao
    abstract fun unidadMedicaDao(): UnidadMedicaDao
    abstract fun medicoPerfilDao(): MedicoPerfilDao
    abstract fun citasMedicoDao(): CitasMedicoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "citas_medicas_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}