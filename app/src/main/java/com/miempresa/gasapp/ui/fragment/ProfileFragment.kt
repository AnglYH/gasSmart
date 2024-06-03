package com.miempresa.gasapp.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.databinding.FragmentProfileBinding
import android.content.Intent
import android.text.Editable
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.miempresa.gasapp.ui.auth.LoginActivity

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val database = Firebase.database
    private val auth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userRef = database.getReference("users").child(currentUser.email!!.replace(".", ","))
            userRef.get().addOnSuccessListener { dataSnapshot ->
                val nombre = dataSnapshot.child("nombre").value as String?
                val phone = dataSnapshot.child("phone").value as String?
                setup(nombre, currentUser.email, phone)
            }.addOnFailureListener {
                // Manejar el error
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun setup(nombre: String?, email: String?, phone: String?) {
        _binding?.let { binding ->
            binding.tvName.text = "Nombre: $nombre"
            binding.tvEmail.text = "Correo: $email"
            binding.etPhone.text = phone?.let { Editable.Factory.getInstance().newEditable(it) }

            binding.btnLogOut.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                val loginIntent = Intent(activity, LoginActivity::class.java)
                startActivity(loginIntent)
            }

            binding.btnApplyChanges.setOnClickListener {
                val newPhone = binding.etPhone.text.toString().trim()

                // Validate new phone number
                if (isValidPhone(newPhone)) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        // Update phone number in Firebase database
                        val userRef = database.getReference("users").child(currentUser.email!!.replace(".", ","))
                        userRef.child("phone").setValue(newPhone).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Show success message
                                Toast.makeText(activity, "Información actualizada exitosamente", Toast.LENGTH_SHORT).show()
                            } else {
                                // Show error message
                                Toast.makeText(activity, "Error al actualizar la información", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // Show error message
                    Toast.makeText(activity, "Por favor, ingresa un número de teléfono válido", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun isValidPhone(phone: String): Boolean {
        return phone.length == 9 && phone.matches(Regex("\\d+"))
    }
}