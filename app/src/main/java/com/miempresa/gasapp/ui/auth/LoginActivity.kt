package com.miempresa.gasapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.miempresa.gasapp.R
import com.miempresa.gasapp.Database.UserDatabase
import com.miempresa.gasapp.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var db: UserDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user)

        db = UserDatabase.getInstance(this)

        editTextUsername = findViewById(R.id.ipt_login_mail)
        editTextPassword = findViewById(R.id.ipt_login_password)
        val buttonLogin = findViewById<Button>(R.id.btn_login)
        val textViewRegister = findViewById<TextView>(R.id.lbl_register)

        buttonLogin.setOnClickListener {
            val email = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            GlobalScope.launch(Dispatchers.IO) {
                val user = db.userDao().getUserByEmail(email)

                if (user != null) {
                    if (user.password == password) {
                        // Credenciales válidas, iniciar sesión
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Contraseña incorrecta
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Usuario no encontrado
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "El usuario no existe, regístrate por favor", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        textViewRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }
    }
}


