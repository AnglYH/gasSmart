package com.miempresa.gasapp.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
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
import com.miempresa.gasapp.network.ApiClient
import com.miempresa.gasapp.network.ApiService
import com.miempresa.gasapp.ui.activity.SensorWifiActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

@Suppress("DEPRECATION")
class WifiInputPasswordDialogFragment : DialogFragment() {
    private val apiService: ApiService by lazy {
        ApiClient.getClient().create(ApiService::class.java)
    }

    // Define una variable para el modo de prueba
    private val isTestMode = false
    private val auth = Firebase.auth
    private var currentUser = auth.currentUser

    // Función para obtener la cantidad de sensores del usuario
    private fun getSensorCount(userId: String, onSuccess: (Int) -> Unit) {
        var sensorCount = 0
        val database = Firebase.database
        val sensorsRef = database.getReference("sensores")
        sensorsRef.get().addOnSuccessListener { dataSnapshot ->
            for (childSnapshot in dataSnapshot.children) {
                val sensorUserId = childSnapshot.child("userId").getValue(String::class.java)
                if (sensorUserId == userId) {
                    sensorCount++
                }
            }
            onSuccess(sensorCount)
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error al leer la base de datos", exception)
        }
    }

    // Función para obtener el ID del usuario autenticado
    private suspend fun obtenerUserId(): String? {
        val snapshot = Firebase.database.getReference("users").child(currentUser?.email!!.replace(".", ",")).get().await()
        return snapshot.child("id").value as String?
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

                        // Recupera el ID del usuario desde Firebase
                        GlobalScope.launch(Dispatchers.IO) {
                            val userId = obtenerUserId()

                            // Obtiene la cantidad de sensores antes de las acciones con el ESP32
                            var initialSensorCount = 0
                            userId?.let {
                                getSensorCount(it) { count ->
                                    initialSensorCount = count
                                }
                            }

                            if (isTestMode) {
                                // Si estás en modo de prueba, muestra el ProgressDialog y redirige al usuario a MainActivity después del retraso
                                withContext(Dispatchers.Main) {
                                    val progressDialog = ProgressDialog(context)
                                    progressDialog.setMessage("Vinculando sensor...")
                                    progressDialog.setCancelable(false)
                                    progressDialog.show()

                                    var finalSensorCount = initialSensorCount
                                    withTimeoutOrNull(20000) {
                                        while (isActive && finalSensorCount <= initialSensorCount) {
                                            delay(1000)  // Espera 1 segundo

                                            // Obtiene la cantidad de sensores después de las acciones con el ESP32
                                            userId?.let {
                                                getSensorCount(it) { count ->
                                                    finalSensorCount = count
                                                }
                                            }
                                        }
                                    }

                                    progressDialog.dismiss()

                                    // Compara las dos cantidades para determinar si la cantidad de sensores ha aumentado
                                    if (finalSensorCount > initialSensorCount) {
                                        Toast.makeText(context, "El sensor se ha agregado correctamente", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Hubo un error al agregar el sensor", Toast.LENGTH_SHORT).show()
                                    }

                                    val intent = Intent(context, MainActivity::class.java)
                                    startActivity(intent)
                                }
                            } else {
                                // Si no estás en modo de prueba, realiza la conexión al dispositivo Bluetooth
                                // Obtén el BluetoothAdapter
                                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

                                // Obtén el BluetoothDevice que representa al sensor
                                // Reemplaza "ESP32GasSmart" con el nombre de tu sensor
                                val sensorDevice = bluetoothAdapter.bondedDevices.find { it.name == "ESP32GasSmart" }

                                sensorDevice?.let { device ->
                                    try {
                                        // Crea un BluetoothSocket y obtén un OutputStream de este
                                        val uuid = device.uuids[0].uuid
                                        val socket = device.createRfcommSocketToServiceRecord(uuid)
                                        socket.connect()
                                        val outputStream = socket.outputStream

                                        // Lee el ID del usuario de la base de datos
                                        val database = Firebase.database
                                        val userIdRef = database.getReference("users/${currentUser?.email!!.replace(".", ",")}/id")
                                        Log.d("Firebase", "userIdRef: $userIdRef")
                                        val dataSnapshot = userIdRef.get().await()
                                        val userId = dataSnapshot.getValue(String::class.java)
                                        Log.d("Firebase", "userId: $userId")
                                        // Escribe los datos del SSID, la contraseña del WiFi y el ID del usuario en el OutputStream
                                        val wifiData = "$userId\n${selectedWifi.SSID}\n$password"
                                        outputStream.write(wifiData.toByteArray())

                                        // Cierra el OutputStream y el BluetoothSocket
                                        outputStream.close()
                                        socket.close()

                                        // Muestra un ProgressDialog
                                        withContext(Dispatchers.Main) {
                                            val progressDialog = ProgressDialog(context)
                                            progressDialog.setMessage("Vinculando sensor ...")
                                            progressDialog.setCancelable(false)
                                            progressDialog.show()

                                            var finalSensorCount = initialSensorCount
                                            withTimeoutOrNull(20000) {
                                                while (isActive && finalSensorCount <= initialSensorCount) {
                                                    delay(1000)  // Espera 1 segundo

                                                    // Obtiene la cantidad de sensores después de las acciones con el ESP32
                                                    userId?.let {
                                                        getSensorCount(it) { count ->
                                                            finalSensorCount = count
                                                        }
                                                    }
                                                }
                                            }

                                            progressDialog.dismiss()

                                            // Obtiene la cantidad de sensores después de las acciones con el ESP32
                                            userId?.let {
                                                getSensorCount(it) { finalSensorCount ->
                                                    // Compara las dos cantidades para determinar si la cantidad de sensores ha aumentado
                                                    if (finalSensorCount > initialSensorCount) {
                                                        Toast.makeText(context, "El sensor se ha agregado correctamente", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Hubo un error al agregar el sensor", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }

                                            val intent = Intent(context, MainActivity::class.java)
                                            startActivity(intent)
                                        }
                                    } catch (e: IOException) {
                                        // Maneja la excepción si no puedes conectarte al dispositivo Bluetooth
                                        Log.e("Bluetooth", "Error al conectarse al dispositivo Bluetooth", e)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "No se pudo conectar al dispositivo Bluetooth", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
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