package com.miempresa.gasapp.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.model.Lectura
import com.miempresa.gasapp.model.Sensor
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.tasks.await

/**
 * SensorViewModel es responsable de preparar y gestionar los datos para las vistas relacionadas con el Sensor.
 * Maneja la comunicación de la aplicación con la base de datos de Firebase.
 */
class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val _sensorData = MutableLiveData<Pair<Sensor?, Lectura?>>()
    val sensorData: LiveData<Pair<Sensor?, Lectura?>> get() = _sensorData

    private val database = Firebase.database

    private val sensorsRef = database.getReference("sensores")
    private val lecturasRef = database.getReference("lecturas")

    // Obtiene los datos del sensor de Firebase y los publica en _sensorData.
    private fun loadSensorData(idSensor: String) {
        viewModelScope.launch {
            if (idSensor == "0") {
                // Si el id del sensor es "0", publica el sensor ficticio directamente
                _sensorData.postValue(Pair(Sensor(id = "0", name = "Registre un sensor", userId = ""), null))
            } else {
                // Si el id del sensor no es "0", busca el sensor en la base de datos
                val lecturaList = getLecturasPorSensor(idSensor)
                val sensorList = getAllSensors()

                val sensor = sensorList.find { it.id == idSensor }
                if (sensor != null) {
                    val lectura = lecturaList.maxByOrNull { it.fechaLectura.toString() }
                    _sensorData.postValue(Pair(sensor, lectura))
                }
            }
        }
    }


    // Inicia el polling de los datos del sensor cada 5 segundos.
    fun startPollingSensorData(idSensor: String) {
        viewModelScope.launch {
            while (isActive) {
                loadSensorData(idSensor)
                delay(5000) // Espera 5 segundos antes de la próxima solicitud
            }
        }
    }

    // Obtiene la lista de lecturas para un sensor de Firebase.
    private suspend fun getLecturasPorSensor(idSensor: String): List<Lectura> {
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

    // Obtiene la lista de todos los sensores de Firebase.
    private suspend fun getAllSensors(): List<Sensor> {
        val snapshot = sensorsRef.get().await()
        return snapshot.children.mapNotNull { it.getValue(Sensor::class.java) }
    }
    // Obtiene el ID del sensor asociado a un usuario de Firebase.
    suspend fun getSensorIdByUserId(userId: String): String? {
        val allSensors = getAllSensors()
        val userSensor = allSensors.find { it.userId == userId }
        return userSensor?.id
    }
}