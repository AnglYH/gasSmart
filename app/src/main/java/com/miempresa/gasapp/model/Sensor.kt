package com.miempresa.gasapp.model

import java.io.Serializable

data class Sensor(
    var id: String = "", // ID del sensor
    var name: String = "",
    var userId: String = "", // ID del usuario que posee el sensor,
    var compraId: String = "", // ID de la compra asociada al sensor
    var notificationSent: Boolean = false,
    var lastNotificationTime: Long = 0 // Hora de la última notificación
) : Serializable