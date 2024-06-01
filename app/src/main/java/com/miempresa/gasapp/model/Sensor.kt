package com.miempresa.gasapp.model

import java.io.Serializable

data class Sensor(
    val idSensor: Int,
    val code: String,
    val gasTankId: Int, // ID del tanque de gas que el sensor está monitoreando
    val idUser: Int,
    val idWifi: Int
) : Serializable