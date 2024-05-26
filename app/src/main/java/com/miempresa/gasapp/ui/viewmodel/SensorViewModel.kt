package com.miempresa.gasapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miempresa.gasapp.model.Lectura
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.network.ApiClient
import com.miempresa.gasapp.network.ApiService
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val _sensorData = MutableLiveData<Pair<Sensor?, Lectura?>>()
    val sensorData: LiveData<Pair<Sensor?, Lectura?>> get() = _sensorData
    private val _idSensor = MutableLiveData<String>()
    val idSensor: LiveData<String> get() = _idSensor

    private val apiService: ApiService by lazy {
        ApiClient.getClient().create(ApiService::class.java)
    }

    fun loadSensorData(idSensor: String) {
        viewModelScope.launch {
            val responseLecturas = apiService.obtenerLecturasPorSensor(idSensor)
            val responseSensores = apiService.obtenerSensores()

            if (responseLecturas.isSuccessful && responseSensores.isSuccessful) {
                val lecturaList = responseLecturas.body()
                val sensorList = responseSensores.body()

                if (!lecturaList.isNullOrEmpty() && !sensorList.isNullOrEmpty()) {
                    val lectura = lecturaList.maxByOrNull { it.fecha }!!
                    val sensor = sensorList.find { it.idSensor == lectura.idSensor }
                    _sensorData.postValue(Pair(sensor, lectura))
                }
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
}