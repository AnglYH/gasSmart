package com.miempresa.gasapp.data

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.model.Sensor
import kotlinx.coroutines.tasks.await

class SensorRepository {
    private val database = Firebase.database
    private val sensorRef = database.getReference("sensors")

    suspend fun insertSensors(sensors: List<Sensor>) {
        sensors.forEach { sensor ->
            sensor.idSensor?.let { id ->
                sensorRef.child(id.toString()).setValue(sensor).await()
            }
        }
    }

    suspend fun getAllSensors(): List<Sensor> {
        val snapshot = sensorRef.get().await()
        return snapshot.children.mapNotNull { it.getValue(Sensor::class.java) }
    }
}