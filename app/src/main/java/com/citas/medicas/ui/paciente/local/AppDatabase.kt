package com.citas.medicas.ui.paciente.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.citas.medicas.ui.paciente.local.dao.CitasDao
import com.citas.medicas.ui.paciente.local.dao.PerfilDao
import com.citas.medicas.ui.paciente.local.dao.UnidadMedicaDao
import com.citas.medicas.ui.paciente.local.entities.CitaEntity
import com.citas.medicas.ui.paciente.local.entities.PerfilEntity
import com.citas.medicas.ui.paciente.local.entities.UnidadMedicaEntity


@Database(
    entities = [CitaEntity::class, PerfilEntity::class, UnidadMedicaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun citasDao(): CitasDao
    abstract fun perfilDao(): PerfilDao
    abstract fun unidadMedicaDao(): UnidadMedicaDao

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