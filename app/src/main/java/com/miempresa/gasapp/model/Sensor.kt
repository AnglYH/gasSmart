package com.miempresa.gasapp.model

import java.io.Serializable

data class Sensor(
    var id: String = "", // ID del sensor
    var name: String = "",
    var userId: String = "", // ID del usuario que posee el sensor,
    var notificationSent: Boolean = false
) : Serializable