package com.miempresa.gasapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RedWifi(
    @SerializedName ("nombre") val nombre: String,
    @SerializedName ("password") val password: String
) : Serializable