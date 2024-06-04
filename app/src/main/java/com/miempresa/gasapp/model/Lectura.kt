package com.miempresa.gasapp.model

data class Lectura(
    var id: String? = null, // Changed from String to Long
    var compraId: String? = null,
    var fechaLectura: String? = null,
    var marcaId: String? = null,
    var porcentajeGas: String? = null,
    var sensorId: String? = null
)