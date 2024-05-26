package com.miempresa.gasapp


import android.app.Application
import androidx.room.Room
import com.miempresa.gasapp.Database.AppDatabase
import com.miempresa.gasapp.model.Distribuidora
import kotlinx.coroutines.runBlocking

class MyAppDistributror : Application() {
    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "db_distributor").build()

        Thread {
            runBlocking {
                // Crear instancias de Distribuidora con los datos correspondientes
                val distribuidora1 = Distribuidora(null, "Distribuidora 1", "Dirección 1", -16.427318, -71.518144)
                val distribuidora2 = Distribuidora(null, "Distribuidora 2", "Dirección 2", -16.429016, -71.523862)
                val distribuidora3 = Distribuidora(null, "Distribuidora 3", "Dirección 3", -16.425723, -71.527022)
                val distribuidora4 = Distribuidora(null, "Distribuidora 4", "Dirección 4", -16.425682, -71.532834)
                val distribuidora5 = Distribuidora(null, "Distribuidora 5", "Dirección 5", -16.423721, -71.532067)

                // Insertar las distribuidoras en la base de datos
                database.distribuidoraDao().insertAll(listOf(distribuidora1, distribuidora2, distribuidora3, distribuidora4, distribuidora5))
            }
        }.start()
    }
}