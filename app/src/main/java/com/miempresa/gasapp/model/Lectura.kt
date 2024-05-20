package com.miempresa.gasapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Lectura(
    @SerializedName("dias_rest") val diasRest: Int,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("porcentaje") val porcentaje: Int,
    @SerializedName("id_sensor") val idSensor: Int
) : Serializable