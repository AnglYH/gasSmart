package com.miempresa.gasapp.model

data class GasBrand(
    val marca_id: Long? = null, // ID único de la marca de gas
    val nombre: String? = null, // Nombre de la marca de gas
    val valvula: Map<String, ValveType>? = null // Mapa de tipos de válvula
)