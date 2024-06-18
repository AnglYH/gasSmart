package com.miempresa.gasapp.data

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.model.Distributor
import kotlinx.coroutines.tasks.await

class DistributorRepository {
    private val database = Firebase.database
    private val distributorRef = database.getReference("distributors")

    suspend fun insertDistributors(distributors: List<Distributor>) {
        distributors.forEach { distributor ->
            distributor.id?.let { id ->
                distributorRef.child(id).setValue(distributor).await()
            }
        }
    }

    suspend fun getAllDistributors(): List<Distributor> {
        val snapshot = distributorRef.get().await()
        return snapshot.children.mapNotNull { it.getValue(Distributor::class.java) }
    }
}