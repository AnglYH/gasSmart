package com.miempresa.gasapp.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.miempresa.gasapp.MainActivity
import com.miempresa.gasapp.R
import com.miempresa.gasapp.ui.activity.SensorWifiActivity

@Suppress("DEPRECATION")
class WifiInputPasswordDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?) : Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val view = inflater.inflate(R.layout.dialog_wifi_input_password, null)
            builder.setView(view)

            val tvWifiSsid = view.findViewById<TextView>(R.id.tv_wifi_ssid)
            val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)
            val etPassword = view.findViewById<EditText>(R.id.et_wifi_password)

            val selectedWifi = arguments?.getParcelable<ScanResult>("selectedWifi")
            tvWifiSsid.text = "Red: ${selectedWifi?.SSID}"


            btnConfirm.setOnClickListener {
                val password = etPassword.text.toString()

                selectedWifi?.let {
                    val wifiManager = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val activity = context as SensorWifiActivity
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        activity.connectToWifi(selectedWifi.wifiSsid.toString(), password)
                    }

                    // Implementación del retardo
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Aquí puedes comprobar si la conexión fue exitosa
                        if (wifiManager.connectionInfo.ssid == String.format("\"%s\"", selectedWifi.SSID)) {
                            Toast.makeText(context, "Conexión exitosa", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(context, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                        }
                    }, 5000) // Retardo de 5 segundos
                }
            }

            val dialog = builder.create()
            dialog?.show()
            return dialog

        } ?: throw IllegalStateException("Activity cannot be null")
    }
}