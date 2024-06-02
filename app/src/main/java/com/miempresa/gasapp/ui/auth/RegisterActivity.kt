package com.miempresa.gasapp.ui.auth

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.MainActivity
import com.miempresa.gasapp.R
import com.miempresa.gasapp.data.UserRepository
import com.miempresa.gasapp.databinding.ActivityRegisterUserBinding
import kotlinx.coroutines.launch
import java.util.regex.Pattern

enum class ProviderType {
    BASIC
}

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterUserBinding
    private val database = Firebase.database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Add TextChangedListener to EditText fields to validate and update colors
        binding.etRegisterMail.addTextChangedListener(createTextWatcher(binding.etRegisterMail, binding.lblMail))
        binding.etRegisterPassword.addTextChangedListener(createTextWatcher(binding.etRegisterPassword, binding.lblRegisterPassword))
        binding.etRegisterPasswordCheck.addTextChangedListener(createTextWatcher(binding.etRegisterPasswordCheck, binding.lblPasswordCheck))
        binding.etRegisterPhone.addTextChangedListener(createTextWatcher(binding.etRegisterPhone, binding.lblPhone))
        binding.etRegisterName.addTextChangedListener(createTextWatcher(binding.etRegisterName, binding.lblName))

        setup()
    }

    private fun setup() {
        title = "Registro de usuario"

        binding.btnRegister.setOnClickListener {
            val email = binding.etRegisterMail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()
            val confirmPassword = binding.etRegisterPasswordCheck.text.toString().trim()
            val phone = binding.etRegisterPhone.text.toString().trim()
            val nombre = binding.etRegisterName.text.toString().trim() // Recoger el nombre del usuario

            // Validate email, password, phone, and name
            if (isValidEmail(email) && isValidPassword(password) && password == confirmPassword && isValidPhone(phone) && nombre.isNotEmpty()) {
                // Register user with UserRepository
                lifecycleScope.launch {
                    val userRepository = UserRepository()
                    val isSuccessful = userRepository.registerUser(email, password, phone, nombre) // Incluir el nombre como argumento
                    if (isSuccessful) {
                        // Show registration successful message
                        Toast.makeText(this@RegisterActivity, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                        // Start main activity
                        showMainActivity(email, ProviderType.BASIC)
                    } else {
                        // Show error message
                        showAlert()
                    }
                }
            } else {
                // If not valid, show error message
                Toast.makeText(this, "Por favor, completa correctamente todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Add OnClickListener to the login text
        binding.lblLogin.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
        }
    }

    private fun showMainActivity(email: String, provider: ProviderType) {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
            putExtra("phone", binding.etRegisterPhone.text.toString().trim())
        }
        startActivity(mainIntent)
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
                    binding.etRegisterMail -> {
                        if (!isValidEmail(text)) {
                            editText.setBackgroundResource(R.drawable.input_background_red)
                            textView.setTextColor(resources.getColor(R.color.red)) // Cambiar color del TextView
                        } else {
                            editText.setBackgroundResource(R.drawable.input_background)
                            textView.setTextColor(resources.getColor(R.color.black)) // Restaurar color del TextView
                        }
                    }
                    binding.etRegisterPassword -> {
                        if (!isValidPassword(text)) {
                            editText.setBackgroundResource(R.drawable.input_background_red)
                            textView.setTextColor(resources.getColor(R.color.red)) // Cambiar color del TextView
                        } else {
                            editText.setBackgroundResource(R.drawable.input_background)
                            textView.setTextColor(resources.getColor(R.color.black)) // Restaurar color del TextView
                        }
                    }
                    binding.etRegisterPasswordCheck -> {
                        val password = binding.etRegisterPassword.text.toString().trim()
                        if (text != password) {
                            editText.setBackgroundResource(R.drawable.input_background_red)
                            textView.setTextColor(resources.getColor(R.color.red)) // Cambiar color del TextView
                        } else {
                            editText.setBackgroundResource(R.drawable.input_background)
                            textView.setTextColor(resources.getColor(R.color.black)) // Restaurar color del TextView
                        }
                    }
                    binding.etRegisterPhone -> {
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

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error registrando el usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}