package com.miempresa.gasapp.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.miempresa.gasapp.model.User

@Dao
interface UserDao {
    @Insert
    fun insert(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    fun getUserByEmailAndPassword(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE phone = :phone")
    fun getUserByPhone(phone: String): User?
}

