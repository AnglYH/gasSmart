package com.miempresa.gasapp.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat

// Esta clase extiende BroadcastReceiver para recibir transmisiones del sistema
class WifiReceiver(private var wifiManager: WifiManager, wifiDeviceList: ListView): BroadcastReceiver() {
    private var sb :StringBuilder? = null
    private var wifiDeviceList :ListView

    // Este método se invoca cuando se recibe una transmisión
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action

        // Comprobamos si la acción de la transmisión es SCAN_RESULTS_AVAILABLE_ACTION
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION == action) {
            sb = StringBuilder()

            context?.let {
                // Comprobamos si se ha concedido el permiso para acceder a la ubicación
                if (ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Obtenemos los resultados de la búsqueda de redes Wi-Fi
                    val wifiList: List<ScanResult> = wifiManager.scanResults
                    val deviceList: ArrayList<String> = ArrayList()

                    // Recorremos los resultados y los añadimos a deviceList
                    for (scanResults in wifiList) {
                        sb?.append("\n")?.append(scanResults.SSID)?.append(" - ")
                            ?.append(scanResults.capabilities)
                        deviceList.add(scanResults.SSID.toString() + " - " + scanResults.capabilities)
                    }

                    // Mostramos un mensaje indicando que las redes se han actualizado
                    Toast.makeText(it, "Redes actualizadas", Toast.LENGTH_SHORT).show()

                    // Creamos un ArrayAdapter y lo establecemos como el adaptador de wifiDeviceList
                    val arrayAdapter: ArrayAdapter<*> =
                        ArrayAdapter(
                            it.applicationContext,
                            android.R.layout.simple_list_item_1,
                            deviceList.toArray()
                        )
                    wifiDeviceList.adapter = arrayAdapter
                } else {
                    // Si no se ha concedido el permiso, mostramos un mensaje indicándolo
                    Toast.makeText(it, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // En el bloque init, inicializamos wifiDeviceList con el valor pasado al constructor de la clase
    init {
        this.wifiDeviceList = wifiDeviceList
    }
}