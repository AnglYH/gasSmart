package com.miempresa.gasapp.network

import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID


class MqttHelper {
    private var mqttClient: MqttClient
    private var isConnected = false

    init {
        val serverUri = "wss://mqtt.flespi.io:443"
        val clientId = "Angel"
        mqttClient = MqttClient(serverUri, clientId, MemoryPersistence())
        connect()
    }

    fun setCallback(callback: MqttCallback) {
        mqttClient.setCallback(callback)
    }

    fun connect() {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        // Configurar el token de Flespi como nombre de usuario
        mqttConnectOptions.userName = "FlespiToken OAyj8LMQreahGxgRENm7sfbWd8bXsOcHhxgGhT5WkH5nCjuR6XuLfnUyy4YOZQmO"

        try {
            if (!mqttClient.isConnected) {
                Log.d("MqttHelper", mqttClient.clientId)
                Log.d("MqttHelper", "Intentando conectar al broker MQTT")
                mqttClient.connect(mqttConnectOptions)
                if (mqttClient.isConnected) {
                    Log.d("MqttHelper", "Conexión MQTT establecida")
                    isConnected = true
                    try {
                        mqttClient.subscribe("esp32GasSmart/gas_control", 0)
                    } catch (ex: MqttException) {
                        Log.e("MqttHelper", "Error al intentar suscribirse al tema", ex)
                    }
                }
            } else {
                Log.d("MqttHelper", "El cliente MQTT ya está conectado")
            }
        } catch (ex: MqttException) {
            Log.e("MqttHelper", "Error al intentar conectar al broker MQTT", ex)
        }
    }

    fun publishMessage(topic: String, switchState: Boolean) {
        if (!mqttClient.isConnected) {
            Log.d("MqttHelper", "Intento de publicar mensaje con conexión MQTT no establecida")
            connect()
            if (!mqttClient.isConnected) {
                Log.e("MqttHelper", "No se pudo restablecer la conexión MQTT")
                return
            }
        }

        try {
            val message = if (switchState) "1" else "0"
            Log.d("MqttHelper", "Publicando mensaje: $message")
            mqttClient.publish(topic, MqttMessage(message.toByteArray()))
        } catch (ex: MqttException) {
            Log.e("MqttHelper", "Error al publicar mensaje", ex)
        }
    }
}