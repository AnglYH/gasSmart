package com.miempresa.gasapp.model

data class Distributor(
    val id: String? = null, // ID único del distribuidor
    val name: String? = null, // Nombre del distribuidor
    val phone: String? = null, // Número de teléfono del distribuidor
    val location: String? = null, // Ubicación del distribuidor en formato "latitud,longitud"
    val address: String? = null, // Dirección del distribuidor
    val latitud: Double? = null, // Latitud del distribuidor
    val longitud: Double? = null, // Longitud del distribuidor
    val marca: List<GasBrand>? = null // Lista de marcas de gas
)