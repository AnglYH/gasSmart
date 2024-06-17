package com.miempresa.gasapp.data

import com.google.firebase.database.FirebaseDatabase
import android.util.Log

/**
 * Esta clase es responsable de gestionar los datos del sensor en Firebase.
 * Proporciona métodos para actualizar el sensor con el ID de la compra y para registrar un sensor para un usuario.
 */
class SensorManager {
    private val database = FirebaseDatabase.getInstance()

    /**
     * Esta función actualiza el sensor con el ID de compra dado.
     * Establece el campo 'compraId' del sensor en Firebase al ID de compra dado.
     *
     * @param id El ID del sensor a actualizar.
     * @param compraId El ID de la compra para asociar con el sensor.
     */
    fun updateSensorWithPurchase(id: String, compraId: String) {
        Log.d("SensorManager", "Actualizando sensor $id con compra $compraId")
        val sensorReference = database.getReference("sensores")
        sensorReference.child(id).child("compraId").setValue(compraId)
    }
}