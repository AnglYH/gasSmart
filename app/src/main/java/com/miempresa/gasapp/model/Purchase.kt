package com.miempresa.gasapp.model

data class Purchase(
    val date: String?, // Fecha de la compra
    val direccion: String?, // Dirección de la compra
    val distributor_Id: String?, // ID del distribuidor donde se realiza la compra
    val id: String?, // ID de la compra
    val marca_id: String?, // ID de la marca del balón de gas que se compra
    val peso_balon: String?, // Peso del balón de gas que se compra
    val price: String?, // Precio del balón de gas que se compra
    val sensor_id: String?, // ID del sensor asociado al usurio en cuestion
    val user_id: String?, // ID del usuario que realiza la compra
    val valvula_balon: String? // ID de la valvula del sensor que se compra
)