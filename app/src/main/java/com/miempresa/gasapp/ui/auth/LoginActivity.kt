package com.miempresa.gasapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.miempresa.gasapp.MainActivity
import com.miempresa.gasapp.databinding.ActivityLoginUserBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginUserBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Verificar si el usuario ya ha iniciado sesión
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Si el usuario ya ha iniciado sesión, redirigir a MainActivity
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish() // Finalizar LoginActivity para que el usuario no pueda volver a ella al presionar el botón de retroceso
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginMail.text.toString().trim()
            val password = binding.etLoginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.lblRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }
    }
}