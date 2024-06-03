package com.miempresa.gasapp.model

data class User(
    val id: String? = null, // UID generado por Firebase
    val nombre: String? = null, // Nombre del usuario
    val password: String? = null, // Contraseña del usuario
    val phone: String? = null, // Número de teléfono del usuario
    val email: String? = null // Correo electrónico del usuario
)