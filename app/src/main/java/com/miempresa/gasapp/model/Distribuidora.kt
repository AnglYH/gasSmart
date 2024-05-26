package com.miempresa.gasapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Distribuidora(
    @PrimaryKey(autoGenerate = true) val id: Int?=null,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "direccion") val direccion: String,
    @ColumnInfo(name = "latitud") val latitud: Double,
    @ColumnInfo(name = "longitud") val longitud: Double
)