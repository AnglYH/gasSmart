package com.miempresa.gasapp.ui.viewmodel

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.R
import com.miempresa.gasapp.model.Lectura
import com.miempresa.gasapp.model.Sensor
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
                _sensorData.postValue(Pair(Sensor(id = "0", name = "No hay un sensor", userId = ""), null))
            } else {
                // Si el id del sensor no es "0", busca el sensor en la base de datos
                val lecturaList = getLecturasPorSensor(idSensor)
                val sensorList = getAllSensors()

                val sensor = sensorList.find { it.id == idSensor }
                if (sensor != null) {
                    val lectura = lecturaList.maxByOrNull { it.fechaLectura.toString() }
                    _sensorData.postValue(Pair(sensor, lectura))

                    val percentage = lectura?.porcentajeGas?.toIntOrNull()
                    if (percentage != null && percentage <= 10) {
                        sendNotification(sensor, percentage)
                    }
                }

            }
        }
    }

    fun startPollingSensorDataHome(sensorIds: List<String>) {
        if (sensorIds.isNotEmpty()) {
            val firstSensorId = sensorIds.first()
            val otherSensorIds = sensorIds.drop(1)

            viewModelScope.launch {
                // Carga la información del primer sensor
                while (isActive) {
                    loadSensorData(firstSensorId)
                    delay(3600000) // Espera 1 hora antes de la próxima solicitud
                }
            }

            viewModelScope.launch {
                delay(500) // Espera un poco antes de cargar los datos de los demás sensores
                // Carga la información del resto de los sensores en paralelo
                otherSensorIds.map { idSensor ->
                    async {
                        while (isActive) {
                            loadSensorData(idSensor)
                            delay(3600000) // Espera 1 hora antes de la próxima solicitud
                        }
                    }
                }.awaitAll()
            }
        }
    }

    fun startPollingSensorDataAdapter(idSensor: String) {
        viewModelScope.launch {
            while (isActive) {
                loadSensorData(idSensor)
                delay(6000)
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

    private fun sendNotification(sensor: Sensor, gasPercentage: Int) {
        // Si ya se envió una notificación para este sensor en los últimos 10 segundos, no hagas nada
        val currentTime = System.currentTimeMillis()
        val lastNotificationTime = getLastNotificationTime(sensor.id)
        //if (currentTime - lastNotificationTime < 10 * 1000) return
        if (currentTime - lastNotificationTime < 60 * 60 * 1000) return

        val context = getApplication<Application>().applicationContext
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        val notification = NotificationCompat.Builder(context, "GasApp")
            .setContentTitle("¡Gas agotandose!")
            .setContentText("${sensor.name} ha registrado un $gasPercentage% de gas restante")
            .setSmallIcon(R.drawable.ic_store_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Utiliza el ID del sensor como el ID de la notificación
        val notificationId = sensor.id.hashCode()
        notificationManager.notify(notificationId, notification)

        // Guarda la hora actual como la hora de la última notificación para este sensor
        saveLastNotificationTime(sensor.id, currentTime)
    }

    private fun saveLastNotificationTime(sensorId: String, time: Long) {
        val sharedPref = getApplication<Application>().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putLong(sensorId, time)
            apply()
        }
    }

    private fun getLastNotificationTime(sensorId: String): Long {
        val sharedPref = getApplication<Application>().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        return sharedPref.getLong(sensorId, 0)
    }
}