package com.miempresa.gasapp.ui.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miempresa.gasapp.MainActivity
import com.miempresa.gasapp.R
import com.miempresa.gasapp.data.SensorRepository
import com.miempresa.gasapp.model.Lectura
import com.miempresa.gasapp.model.Sensor
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class SensorViewModel(application: Application, private val sensorRepository: SensorRepository) : AndroidViewModel(application) {
    private val _sensorData = MutableLiveData<Pair<Sensor?, Lectura?>>()
    val sensorData: LiveData<Pair<Sensor?, Lectura?>> get() = _sensorData

    private fun loadSensorData(idSensor: String) {
        viewModelScope.launch {
            if (idSensor == "0") {
                _sensorData.postValue(Pair(Sensor(id = "0", name = "No hay un sensor", userId = ""), null))
            } else {
                val lecturaList = sensorRepository.getLecturasPorSensor(idSensor)
                val sensorList = sensorRepository.getAllSensors()

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
                while (isActive) {
                    loadSensorData(firstSensorId)
                    delay(3600000)
                }
            }

            viewModelScope.launch {
                delay(500)
                otherSensorIds.map { idSensor ->
                    launch {
                        while (isActive) {
                            loadSensorData(idSensor)
                            delay(3600000)
                        }
                    }
                }
            }
        }
    }

    fun startPollingSensorDataAdapter(idSensor: String) {
        viewModelScope.launch {
            while (isActive) {
                loadSensorData(idSensor)
                delay(5000)
            }
        }
    }

    suspend fun getSensorIdByUserId(userId: String): String? {
        val sensors = sensorRepository.getAllSensors()
        return sensors.find { it.userId == userId }?.id
    }

    suspend fun getAverageChangeRate(sensorId: String): Float {
        val lecturas = sensorRepository.getLecturasPorSensor(sensorId)
        val sortedLecturas = lecturas.sortedByDescending { it.fechaLectura }
        val filteredLecturas = mutableListOf<Lectura>()

        for (i in sortedLecturas.indices) {
            val lectura1 = sortedLecturas[i]
            val lectura2 = sortedLecturas.getOrNull(i + 1) ?: Lectura(porcentajeGas = "0")

            val porcentajeGas1 = lectura1.porcentajeGas?.toFloatOrNull()
            val porcentajeGas2 = lectura2.porcentajeGas?.toFloatOrNull()

            if (porcentajeGas1 != null && porcentajeGas2 != null && porcentajeGas2 < porcentajeGas1) {
                filteredLecturas.add(lectura1)
                break
            }

            filteredLecturas.add(lectura1)
        }

        val finalLecturas = filteredLecturas.sortedBy { it.fechaLectura }

        if (finalLecturas.size == 1 && finalLecturas[0].porcentajeGas == "100") {
            return 30f
        }

        val changeRates = mutableListOf<Float>()

        for (i in 0 until finalLecturas.size - 1) {
            val lectura1 = finalLecturas[i]
            val lectura2 = finalLecturas[i + 1]

            val porcentajeGas1 = lectura1.porcentajeGas?.toFloatOrNull()
            val porcentajeGas2 = lectura2.porcentajeGas?.toFloatOrNull()

            if (porcentajeGas1 != null && porcentajeGas2 != null) {
                val changeRate = Math.abs(porcentajeGas2 - porcentajeGas1)
                changeRates.add(changeRate)
            }
        }

        val averageChangeRate = changeRates.average().toFloat()
        val currentGasPercentage = finalLecturas.last().porcentajeGas?.toFloatOrNull() ?: 0f

        val remainingDays = currentGasPercentage / averageChangeRate

        return remainingDays
    }

    private fun sendNotification(sensor: Sensor, gasPercentage: Int) {
        val currentTime = System.currentTimeMillis()
        val lastNotificationTime = getLastNotificationTime(sensor.id)
        //if (currentTime - lastNotificationTime < 10 * 1000) return
        if (currentTime - lastNotificationTime < 60 * 60 * 1000) return

        val context = getApplication<Application>().applicationContext
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        // Define el ID del canal de notificación aquí
        val channelId = "GasApp_Channel"

        // Crea un canal de notificación
        val name = "GasApp Channel"
        val descriptionText = "Channel for GasApp notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)

        // Crea un intent para el toque de notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_store_icon)
            .setContentTitle("¡El gas se agota!")
            .setContentText("El sensor ${sensor.name} tiene un $gasPercentage% de gas restante")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Establece el intent que se dispara cuando el usuario toca la notificación
            .setAutoCancel(true) // Elimina automáticamente la notificación cuando el usuario la toca
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Establece la visibilidad de la notificación
            .build()

        val notificationId = sensor.id.hashCode()
        notificationManager.notify(notificationId, notification)

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