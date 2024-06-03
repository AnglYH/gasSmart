package com.miempresa.gasapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.data.SensorRepository
import com.miempresa.gasapp.model.Lectura
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.model.User
import com.miempresa.gasapp.network.ApiClient
import com.miempresa.gasapp.network.ApiService
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.tasks.await

class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val _sensorData = MutableLiveData<Pair<Sensor?, Lectura?>>()
    val sensorData: LiveData<Pair<Sensor?, Lectura?>> get() = _sensorData

    private val database = Firebase.database

    private val sensorsRef = database.getReference("sensores")
    private val lecturasRef = database.getReference("lecturas")

    // Carga el ID del sensor
    private fun loadSensorData(idSensor: String) {
        viewModelScope.launch {
            val lecturaList = getLecturasPorSensor(idSensor)
            val sensorList = getAllSensors()

            if (lecturaList.isNotEmpty() && sensorList.isNotEmpty()) {
                val sensor = sensorList.find { it.id == idSensor }
                val lectura = lecturaList.maxByOrNull { it.fecha_lectura.toString() }

                _sensorData.postValue(Pair(sensor, lectura))
            }

        }
    }


    // Inicia el polling de los datos del sensor
    fun startPollingSensorData(idSensor: String) {
        viewModelScope.launch {
            while (isActive) {
                loadSensorData(idSensor)
                delay(5000) // Espera 5 segundos antes de la próxima solicitud
            }
        }
    }

    private suspend fun getLecturasPorSensor(idSensor: String): List<Lectura> {
        val snapshot = lecturasRef.get().await()
        return snapshot.children.mapNotNull { dataSnapshot ->
            val lectura = dataSnapshot.getValue(Lectura::class.java)
            // Filtra las lecturas por el ID del sensor
            if (lectura != null && lectura.sensor_id == idSensor) {
                lectura.id = dataSnapshot.key?.toLong() // Asigna el ID de la lectura
                lectura
            } else {
                null
            }
        }
    }

    private suspend fun getAllSensors(): List<Sensor> {
        val snapshot = sensorsRef.get().await()
        return snapshot.children.mapNotNull { it.getValue(Sensor::class.java) }
    }
}