package com.miempresa.gasapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Sensor(
    @SerializedName("id") val idSensor: Int,
    @SerializedName("nombre") val code: String,
    @SerializedName("id_balon") val idGasTank: Int,
    @SerializedName("id_usuario") val idUser: Int
) : Serializable