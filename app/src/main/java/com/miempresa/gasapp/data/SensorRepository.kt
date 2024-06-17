package com.miempresa.gasapp.data

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.model.Lectura
import com.miempresa.gasapp.model.Sensor
import kotlinx.coroutines.tasks.await

class SensorRepository {
    private val database = Firebase.database
    private val sensorRef = database.getReference("sensores")
    private val lecturasRef = database.getReference("lecturas")

    suspend fun insertSensors(sensors: List<Sensor>) {
        sensors.forEach { sensor ->
            sensor.id.let { id ->
                sensorRef.child(id.toString()).setValue(sensor).await()
            }
        }
    }

    suspend fun getAllSensors(): List<Sensor> {
        val snapshot = sensorRef.get().await()
        return snapshot.children.mapNotNull { it.getValue(Sensor::class.java) }
    }

    suspend fun getLecturasPorSensor(idSensor: String): List<Lectura> {
        val snapshot = lecturasRef.get().await()
        return snapshot.children.mapNotNull { dataSnapshot ->
            try {
                val lectura = dataSnapshot.getValue(Lectura::class.java)
                if (lectura != null && lectura.sensorId == idSensor) {
                    lectura.id = dataSnapshot.key
                    lectura
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error al deserializar la lectura con ID: ${dataSnapshot.key}", e)
                null
            }
        }
    }
}