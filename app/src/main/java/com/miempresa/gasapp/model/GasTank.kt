package com.miempresa.gasapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GasTank(
    @PrimaryKey(autoGenerate = true) val id: Int?=null,
    @ColumnInfo(name = "marca") val marca: String,
    @ColumnInfo(name = "valvula") val valvula: String,
    @ColumnInfo(name = "peso") val peso: String
)

