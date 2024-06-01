package com.miempresa.gasapp.data

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.model.Purchase
import kotlinx.coroutines.tasks.await

class PurchaseRepository {
    private val database = Firebase.database
    private val purchaseRef = database.getReference("purchases")

    suspend fun insertPurchase(purchase: Purchase) {
        purchase.purchaseId?.let { id ->
            purchaseRef.child(id).setValue(purchase).await()
        }
    }

    suspend fun getAllPurchases(): List<Purchase> {
        val snapshot = purchaseRef.get().await()
        return snapshot.children.mapNotNull { it.getValue(Purchase::class.java) }
    }
}