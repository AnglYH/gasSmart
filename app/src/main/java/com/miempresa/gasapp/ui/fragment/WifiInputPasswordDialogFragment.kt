package com.miempresa.gasapp.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.miempresa.gasapp.MainActivity
import com.miempresa.gasapp.R
import com.miempresa.gasapp.model.RedWifi
import com.miempresa.gasapp.network.ApiClient
import com.miempresa.gasapp.network.ApiService
import com.miempresa.gasapp.ui.activity.SensorWifiActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Suppress("DEPRECATION")
class WifiInputPasswordDialogFragment : DialogFragment() {
    private val apiService: ApiService by lazy {
        ApiClient.getClient().create(ApiService::class.java)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?) : Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val view = inflater.inflate(R.layout.dialog_wifi_input_password, null)
            builder.setView(view)

            val tvWifiSsid = view.findViewById<TextView>(R.id.tv_wifi_ssid)
            val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)
            val tvWifiPassword = view.findViewById<TextView>(R.id.tv_wifi_password)
            val etPassword = view.findViewById<EditText>(R.id.et_wifi_password)

            val selectedWifi = arguments?.getParcelable<ScanResult>("selectedWifi")
            tvWifiSsid.text = "Red: ${selectedWifi?.SSID}"

            btnConfirm.setOnClickListener {
                val password = etPassword.text.toString()

                if (password.isEmpty()) {
                    // Cambia el color del campo de texto a rojo
                    etPassword.setBackgroundResource(R.drawable.input_background_red)
                    tvWifiPassword.setTextColor(Color.RED)

                    // Muestra un mensaje de error
                    Toast.makeText(context, "Por favor, ingresa una contraseña", Toast.LENGTH_SHORT).show()
                } else {
                    selectedWifi?.let {
                        val wifiManager = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        val activity = context as SensorWifiActivity
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            activity.connectToWifi(selectedWifi.wifiSsid.toString(), password)
                        }

                        // Obtén el BluetoothAdapter
                        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

                        // Obtén el BluetoothDevice que representa al sensor
                        // Reemplaza "ESP32GasSmart" con el nombre de tu sensor
                        val sensorDevice = bluetoothAdapter.bondedDevices.find { it.name == "ESP32GasSmart" }

                        sensorDevice?.let { device ->
                            // Crea un BluetoothSocket y obtén un OutputStream de este
                            val uuid = device.uuids[0].uuid
                            val socket = device.createRfcommSocketToServiceRecord(uuid)
                            socket.connect()
                            val outputStream = socket.outputStream

                            // Recupera el ID del usuario desde Firebase
                            val user = FirebaseAuth.getInstance().currentUser
                            val currentUserEmail = user?.email?.replace(".", ",")
                            val database = FirebaseDatabase.getInstance()
                            val userIdRef = database.getReference("users/${currentUserEmail}/id")
                            Log.d("Firebase", "userIdRef: $userIdRef")

                            // Lee el ID del usuario de la base de datos
                            userIdRef.get().addOnSuccessListener { dataSnapshot ->
                                val userId = dataSnapshot.getValue(String::class.java)
                                    Log.d("Firebase", "userId: $userId")
                                // Escribe los datos del SSID, la contraseña del WiFi y el ID del usuario en el OutputStream
                                val wifiData = "$userId\n${selectedWifi.SSID}\n$password"
                                outputStream.write(wifiData.toByteArray())

                                // Cierra el OutputStream y el BluetoothSocket
                                outputStream.close()
                                socket.close()
                            }.addOnFailureListener { exception ->
                                // Maneja cualquier error que ocurra al leer la base de datos
                                Log.e("Firebase", "Error al leer la base de datos", exception)
                            }
                        }
                    }
                }
            }

            val dialog = builder.create()
            dialog?.show()
            return dialog

        } ?: throw IllegalStateException("Activity cannot be null")
    }
}