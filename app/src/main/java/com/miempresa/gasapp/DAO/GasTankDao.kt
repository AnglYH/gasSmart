package com.miempresa.gasapp.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.miempresa.gasapp.model.GasTank

@Dao
interface GasTankDao {
    @Query("SELECT * FROM gasTank")
    fun getAll(): List<GasTank>
    @Insert
    fun insert(gasTank: GasTank)
}