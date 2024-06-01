package com.miempresa.gasapp.data

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.model.GasTank
import kotlinx.coroutines.tasks.await

class GasTankRepository {
    private val database = Firebase.database
    private val gasTankRef = database.getReference("gasTanks")

    suspend fun insertGasTanks(gasTanks: List<GasTank>) {
        gasTanks.forEach { gasTank ->
            gasTank.gasTankId?.let { id ->
                gasTankRef.child(id).setValue(gasTank).await()
            }
        }
    }

    suspend fun getAllGasTanks(): List<GasTank> {
        val snapshot = gasTankRef.get().await()
        return snapshot.children.mapNotNull { it.getValue(GasTank::class.java) }
    }

    suspend fun getGasTank(gasTankId: String?): GasTank? {
        val snapshot = gasTankRef.child(gasTankId ?: return null).get().await()
        return snapshot.getValue(GasTank::class.java)
    }
}