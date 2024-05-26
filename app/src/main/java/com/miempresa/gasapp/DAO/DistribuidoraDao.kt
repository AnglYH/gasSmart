package com.miempresa.gasapp.DAO
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.miempresa.gasapp.model.Distribuidora

//getAll es un método que devuelve todas las distribuidoras de la base de datos,
// y insertAll es un método que inserta una lista de distribuidoras en la base de datos.
// Si ya existe una distribuidora con el mismo id, se reemplaza.
@Dao

interface DistribuidoraDao {
    @Query("SELECT * FROM Distribuidora")
    suspend fun getAll(): List<Distribuidora>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(distribuidoras: List<Distribuidora>)
}
