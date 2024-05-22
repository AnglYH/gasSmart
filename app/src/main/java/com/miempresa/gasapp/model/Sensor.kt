package com.miempresa.gasapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sensor(
    val id: Int,
    val code: String,
) : Parcelable