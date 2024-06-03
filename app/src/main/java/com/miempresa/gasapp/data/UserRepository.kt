package com.miempresa.gasapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = Firebase.database
    private val userRef = database.getReference("users")

    suspend fun registerUser(email: String, password: String, telefono: String, nombre: String?): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = User(
                id = result.user?.uid,
                password = password,
                phone = telefono,
                email = email,
                nombre = nombre
            )
            userRef.child(email.replace(".", ",")).setValue(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun loginUser(email: String, password: String): Boolean {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user != null
        } catch (e: Exception) {
            false
        }
    }
}