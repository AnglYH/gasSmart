package com.miempresa.gasapp.model

data class Lectura(
    val diasRest: Int, // representa los dias que le quedan al usuario
    val fecha: String, //representa la fecha de la lectura
    val porcentaje: Int, // representa el porcentaje de gas que le quedan al usuario
    val idSensor: Int, // representa el id del sensor
    val idGasTank: Int?, // reprensta el id del tanque de gas
    val estado: String, // reprensenta el estado del sensor
    val purchaseId: Int? // representa el id de la compra
)