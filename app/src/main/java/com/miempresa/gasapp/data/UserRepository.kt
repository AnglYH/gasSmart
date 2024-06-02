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

    suspend fun registerUser(email: String, password: String, telefono: String, name: String? = null, address: String? = null): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = User(
                id = result.user?.uid,
                password = password,
                phone = telefono,
                email = email,
                address = address,
            )
            userRef.child(user.id!!).setValue(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        var isSuccessful = false
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            isSuccessful = task.isSuccessful
        }.await()
        return isSuccessful
    }
}