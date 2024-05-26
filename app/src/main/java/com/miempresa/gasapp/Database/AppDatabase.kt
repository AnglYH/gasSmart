package com.miempresa.gasapp.Database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.miempresa.gasapp.DAO.DistribuidoraDao
import com.miempresa.gasapp.model.Distribuidora

@Database(entities = [Distribuidora::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun distribuidoraDao(): DistribuidoraDao

}