package com.miempresa.gasapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserAPI(
    @SerializedName("id") val idUser: Int,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("phone") val phone: String
) : Serializable