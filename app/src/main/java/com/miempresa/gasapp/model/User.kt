package com.miempresa.gasapp.model

import androidx.room.Entity
import java.io.Serializable

data class User(
    val id: String?,
    val password: String?,
    val phone: String?
)