package com.miempresa.gasapp.model


data class User(
    val id: String?, // UID generado por Firebase
    val password: String?, // Contraseña del usuario
    val phone: String?, // Número de teléfono del usuario
    val email: String?, // Correo electrónico del usuario
    val address: String?, // Dirección del usuario
)