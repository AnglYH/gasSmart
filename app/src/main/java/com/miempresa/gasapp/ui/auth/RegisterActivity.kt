package com.miempresa.gasapp.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.miempresa.gasapp.DAO.UserDao
import com.miempresa.gasapp.Database.UserDatabase
import com.miempresa.gasapp.MainActivity
import com.miempresa.gasapp.R
import com.miempresa.gasapp.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var buttonRegister: Button
    private lateinit var textViewEmail: TextView
    private lateinit var textViewPassword: TextView
    private lateinit var textViewConfirmPassword: TextView
    private lateinit var textViewPhone: TextView

    private lateinit var db: UserDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_user)

        // Initialize database and DAO
        db = UserDatabase.getInstance(applicationContext)
        userDao = db.userDao()

        // Find views by their IDs
        editTextEmail = findViewById(R.id.ipt_register_mail)
        editTextPassword = findViewById(R.id.ipt_register_password)
        editTextConfirmPassword = findViewById(R.id.ipt_register_password_check)
        editTextPhone = findViewById(R.id.ipt_register_phone)
        buttonRegister = findViewById(R.id.btn_register)
        textViewEmail = findViewById(R.id.lbl_mail)
        textViewPassword = findViewById(R.id.lbl_contraseña)
        textViewConfirmPassword = findViewById(R.id.lbl_password_check)
        textViewPhone = findViewById(R.id.lbl_phone)

        // Add TextChangedListener to EditText fields to validate and update colors
        editTextEmail.addTextChangedListener(createTextWatcher(editTextEmail, textViewEmail))
        editTextPassword.addTextChangedListener(createTextWatcher(editTextPassword, textViewPassword))
        editTextConfirmPassword.addTextChangedListener(createTextWatcher(editTextConfirmPassword, textViewConfirmPassword))
        editTextPhone.addTextChangedListener(createTextWatcher(editTextPhone, textViewPhone))

        // Add OnClickListener to the register button
        buttonRegister.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()
            val phone = editTextPhone.text.toString().trim()

            // Validate email, password, and phone
            if (isValidEmail(email) && isValidPassword(password) && password == confirmPassword && isValidPhone(phone)) {
                // Check if email already exists
                GlobalScope.launch(Dispatchers.IO) {
                    val existingUser = userDao.getUserByEmail(email)
                    if (existingUser != null) {
                        // Email already exists, show error message
                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, "El correo electrónico ya está registrado", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Email does not exist, proceed with registration
                        userDao.insert(User(email = email, password = password, phone = phone))
                        // Show registration successful message
                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                            // Start main activity or any other activity
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            } else {
                // If not valid, show error message
                Toast.makeText(this, "Por favor, completa correctamente todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to create TextWatcher for EditText fields
    private fun createTextWatcher(editText: EditText, textView: TextView): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()
                // Validate and update color of EditText field
                when (editText) {
                    editTextEmail -> {
                        if (!isValidEmail(text)) {
                            editText.setBackgroundResource(R.drawable.input_background_red)
                            textView.setTextColor(resources.getColor(R.color.red)) // Cambiar color del TextView
                        } else {
                            editText.setBackgroundResource(R.drawable.input_background)
                            textView.setTextColor(resources.getColor(R.color.black)) // Restaurar color del TextView
                        }
                    }
                    editTextPassword -> {
                        if (!isValidPassword(text)) {
                            editText.setBackgroundResource(R.drawable.input_background_red)
                            textView.setTextColor(resources.getColor(R.color.red)) // Cambiar color del TextView
                        } else {
                            editText.setBackgroundResource(R.drawable.input_background)
                            textView.setTextColor(resources.getColor(R.color.black)) // Restaurar color del TextView
                        }
                    }
                    editTextConfirmPassword -> {
                        val password = editTextPassword.text.toString().trim()
                        if (text != password) {
                            editText.setBackgroundResource(R.drawable.input_background_red)
                            textView.setTextColor(resources.getColor(R.color.red)) // Cambiar color del TextView
                        } else {
                            editText.setBackgroundResource(R.drawable.input_background)
                            textView.setTextColor(resources.getColor(R.color.black)) // Restaurar color del TextView
                        }
                    }
                    editTextPhone -> {
                        if (!isValidPhone(text)) {
                            editText.setBackgroundResource(R.drawable.input_background_red)
                            textView.setTextColor(resources.getColor(R.color.red)) // Cambiar color del TextView
                        } else {
                            editText.setBackgroundResource(R.drawable.input_background)
                            textView.setTextColor(resources.getColor(R.color.black)) // Restaurar color del TextView
                        }
                    }
                }
            }
        }
    }

    // Function to validate email
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Function to validate password
    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}\$")
        return passwordPattern.matcher(password).matches()
    }

    // Function to validate phone number
    private fun isValidPhone(phone: String): Boolean {
        return phone.length == 9 && phone.matches(Regex("\\d+"))
    }
}


