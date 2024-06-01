package com.miempresa.gasapp

import android.app.Application
import com.miempresa.gasapp.data.DistributorRepository
import com.miempresa.gasapp.model.Distributor
import kotlinx.coroutines.runBlocking

class MyAppDistributror : Application() {
    override fun onCreate() {
        super.onCreate()

        val repository = DistributorRepository()

        Thread {
            runBlocking {
                // Crear instancias de Distributor con los datos correspondientes
                val distributor1 = Distributor("1", "Distribuidora 1", "963258741", "-16.427318,-71.518144", "Dirección 1", -16.427318, -71.518144)
                val distributor2 = Distributor("2", "Distribuidora 2", "741258963", "-16.429016,-71.523862", "Dirección 2", -16.429016, -71.523862)
                val distributor3 = Distributor("3", "Distribuidora 3", "852147963", "-16.425723,-71.527022", "Dirección 3", -16.425723, -71.527022)
                val distributor4 = Distributor("4", "Distribuidora 4", "789456123", "-16.425682,-71.532834", "Dirección 4", -16.425682, -71.532834)
                val distributor5 = Distributor("5", "Distribuidora 5", "321456789", "-16.423721,-71.532067", "Dirección 5", -16.423721, -71.532067)

                // Insertar las distribuidoras en Firebase Realtime Database
                repository.insertDistributors(listOf(distributor1, distributor2, distributor3, distributor4, distributor5))
            }
        }.start()
    }
}