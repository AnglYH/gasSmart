package com.miempresa.gasapp.model

data class Purchase(
    val purchaseId: String?, // ID único de la compra
    val userId: String?, // ID del usuario que realiza la compra
    val distributorId: String?, // ID del distribuidor donde se realiza la compra
    val gasTankId: String? // ID del balón de gas que se compra
)
