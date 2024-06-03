package com.miempresa.gasapp.model

data class Lectura(
    var id: Long? = null, // Changed from String to Long
    var compra_id: Long? = null,
    var fecha_lectura: String? = null,
    var marca_id: Long? = null,
    var porcentaje_gas: Long? = null,
    var sensor_id: String? = null
)