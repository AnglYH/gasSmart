package com.miempresa.gasapp.Database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.miempresa.gasapp.DAO.GasTankDao
import com.miempresa.gasapp.model.GasTank

@Database(entities = [GasTank::class], version = 2)
abstract class GasTankDatabase: RoomDatabase() {
    abstract fun gasTankDao(): GasTankDao
}