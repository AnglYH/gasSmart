package com.miempresa.gasapp.data

import com.miempresa.gasapp.MyAppDistributror
import com.miempresa.gasapp.model.Distribuidora
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
// esta es la clase para
class DistribuidoraRepository {

    suspend fun insertDistribuidoras(distribuidoras: List<Distribuidora>) {
        withContext(Dispatchers.IO) {
            MyAppDistributror.database.distribuidoraDao().insertAll(distribuidoras)
        }
    }

    suspend fun getAllDistribuidoras(): List<Distribuidora> {
        return withContext(Dispatchers.IO) {
            MyAppDistributror.database.distribuidoraDao().getAll()
        }
    }
}