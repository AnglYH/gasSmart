package com.miempresa.gasapp.model
// EVALUAR SI SE VA USAR ESTE MODELO
data class GasTank(
    val gasTankId: String?, // ID único del balón de gas
    val distributorId: String?, // ID del distribuidor que maneja este balón de gas
    val gasBrand: String?, // Marca de gas
    val valveType: String?, // Tipo de válvula
    val gasWeight: Int?, // Peso del balón de gas en kg
    val price: Double?, // Precio del balón de gas
)


// cada dato del balon de gas debe estar asociado   la marca de gas del distribuidor
// manejo de datos de precio por distribuidora
// precio debe estar asociado al balon de gas
// el balon de gas y su precio debe de estar asociado al distribuidor
// ya que estos pueden majear precios pesrsonalizados por cada presenetacion de gas