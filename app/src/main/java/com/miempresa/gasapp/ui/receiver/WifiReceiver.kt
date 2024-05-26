package com.miempresa.gasapp.ui.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.miempresa.gasapp.adapter.WifiListAdapter

// Esta clase extiende BroadcastReceiver para recibir transmisiones del sistema
class WifiReceiver(private var wifiManager: WifiManager, private val wifiDeviceList: ListView, private val context: Context): BroadcastReceiver() {
    // Lista para almacenar los resultados del escaneo de WiFi
    private var wifiList: List<ScanResult> = listOf()

    // Método que se llama cuando se recibe una transmisión
    override fun onReceive(context: Context?, intent: Intent?) {
        // Obtenemos la acción de la intención
        val action = intent?.action

        // Comprobamos si la acción es SCAN_RESULTS_AVAILABLE_ACTION, lo que significa que los resultados del escaneo de WiFi están disponibles
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION == action) {
            if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Obtenemos los resultados del escaneo de WiFi
                wifiList = wifiManager.scanResults

                // Creamos un adaptador con los resultados del escaneo de WiFi y lo establecemos en la lista de dispositivos WiFi
                val wifiListAdapter = WifiListAdapter(this.context, wifiList)
                    wifiDeviceList.adapter = wifiListAdapter
            } else {
                // Si no se ha concedido el permiso de ubicación, mostramos un mensaje
                Toast.makeText(context, "No se han concedido permisos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para obtener la lista de resultados del escaneo de WiFi
    fun getWifiList(): List<ScanResult> {
        return wifiList
    }
}