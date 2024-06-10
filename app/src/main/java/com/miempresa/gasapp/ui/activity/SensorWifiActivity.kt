@file:Suppress("DEPRECATION")

package com.miempresa.gasapp.ui.activity

import WifiStateReceiver
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.miempresa.gasapp.R
import com.miempresa.gasapp.databinding.ActivitySensorWifiBinding
import com.miempresa.gasapp.ui.receiver.WifiReceiver
import com.miempresa.gasapp.ui.dialog.AyudaWifiDialogFragment
import com.miempresa.gasapp.ui.fragment.WifiInputPasswordDialogFragment
import android.provider.Settings

@Suppress("DEPRECATION")
class SensorWifiActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySensorWifiBinding
    private var wifiManager : WifiManager? = null
    private var wifiList : ListView? = null
    private val MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1
    private var receiverWifi : WifiReceiver? = null

    private lateinit var wifiStateReceiver: WifiStateReceiver

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySensorWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        wifiStateReceiver = WifiStateReceiver(binding)
        registerReceiver(wifiStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_sensor_wifi)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiList = binding.lvWifi

        // Solicita el permiso de ubicación precisa
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_ACCESS_FINE_LOCATION
            )
        }

        binding.btnSearchWifi.setOnClickListener {
            if (wifiManager!!.isWifiEnabled) {
                scanForWifiNetworks()
                Toast.makeText(this, "Buscando redes WiFi...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, enciende el Wi-Fi antes de escanear", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivQuestion.setOnClickListener {
            val dialog = AyudaWifiDialogFragment()
            dialog.show(supportFragmentManager, "dialog_wifi")
        }

        // Establece un listener para el evento de clic en un elemento de la lista de redes WiFi encontradas
        wifiList!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedWifi = receiverWifi?.getWifiList()?.get(position)
            if (selectedWifi != null) {
                val passwordWifiDialog = WifiInputPasswordDialogFragment()

                val args = Bundle()
                args.putParcelable("selectedWifi", selectedWifi)
                passwordWifiDialog.arguments = args
                passwordWifiDialog.show(supportFragmentManager, "dialog_wifi_password")
            }
        }

        // Establece un listener para el evento de clic en el botón de encendido/apagado del Wi-Fi
        binding.ivToggleWifi.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onPostResume() {
        super.onPostResume()

        wifiManager?.let { manager ->
            wifiList?.let { list ->
                receiverWifi = WifiReceiver(manager, list, this)
                registerReceiver(receiverWifi, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (receiverWifi != null) {
            unregisterReceiver(receiverWifi)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiStateReceiver)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MY_PERMISSIONS_ACCESS_FINE_LOCATION
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(this, "Permiso no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scanForWifiNetworks() {
        wifiManager?.startScan()
    }
    fun connectToWifi(ssid: String, password: String) {
        val wifiConfig = WifiConfiguration().apply {
            SSID = "\"" + ssid + "\""
            preSharedKey = "\"" + password + "\""
        }

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val netId = wifiManager.addNetwork(wifiConfig)
        wifiManager.disconnect()
        wifiManager.enableNetwork(netId, true)
        wifiManager.reconnect()
    }
}