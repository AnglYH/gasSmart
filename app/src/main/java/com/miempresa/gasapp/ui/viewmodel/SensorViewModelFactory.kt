package com.miempresa.gasapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miempresa.gasapp.data.SensorRepository

@Suppress("UNCHECKED_CAST")
class SensorViewModelFactory(private val application: Application, private val sensorRepository: SensorRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorViewModel::class.java)) {
            return SensorViewModel(application, sensorRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}