package com.miempresa.gasapp.ui.activity

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.miempresa.gasapp.R
import com.miempresa.gasapp.databinding.ActivitySensorWifiBinding
import com.miempresa.gasapp.ui.WifiReceiver
import com.miempresa.gasapp.ui.dialog.AyudaWifiDialogFragment

// Esta es la actividad principal que maneja la funcionalidad de escaneo de WiFi
class SensorWifiActivity : AppCompatActivity() {

    // Declaración de variables
    private lateinit var binding: ActivitySensorWifiBinding
    private var wifiManager : WifiManager? = null
    private var wifiList : ListView? = null
    private val MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1
    private var receiverWifi : WifiReceiver? = null

    // Método onCreate que se llama cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySensorWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la vista para permitir el diseño de borde a borde
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_sensor_wifi)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización del administrador de WiFi y la lista de WiFi
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiList = binding.lvWifi

        // Comprobación y activación del WiFi si está desactivado
        if (wifiManager?.isWifiEnabled == false) {
            Toast.makeText(this, "Encendiendo wifi...", Toast.LENGTH_SHORT).show()
            wifiManager?.isWifiEnabled = true
        }

        // Comprobación y solicitud de permisos de ubicación si no se han concedido
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                MY_PERMISSIONS_ACCESS_COARSE_LOCATION
            )
        }

        // Configuración del botón de búsqueda de WiFi para iniciar el escaneo cuando se hace clic
        binding.btnSearchWifi.setOnClickListener {
            wifiManager?.startScan()
        }

        // Configuración del botón de ayuda para mostrar un diálogo cuando se hace clic
        binding.ivQuestion.setOnClickListener {
            val dialog = AyudaWifiDialogFragment()
            dialog.show(supportFragmentManager, "dialog_wifi")
        }
    }

    // Método onPostResume que se llama cuando la actividad vuelve a estar en primer plano
    override fun onPostResume() {
        super.onPostResume()

        // Registro del receptor de WiFi para recibir actualizaciones de la búsqueda de WiFi
        wifiManager?.let { manager ->
            wifiList?.let { list ->
                receiverWifi = WifiReceiver(manager, list)
                registerReceiver(receiverWifi, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            }
        }
    }

    // Método onPause que se llama cuando la actividad ya no está en primer plano
    override fun onPause() {
        super.onPause()

        // Anulación del registro del receptor de WiFi cuando la actividad ya no está en primer plano
        if (receiverWifi != null) {
            unregisterReceiver(receiverWifi)
        }
    }

    // Método que se llama cuando el usuario ha respondido a la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Comprobación de si se ha concedido el permiso de ubicación y muestra de un mensaje en consecuencia
        if (requestCode == MY_PERMISSIONS_ACCESS_COARSE_LOCATION
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(this, "Permiso no concedido", Toast.LENGTH_SHORT).show()
        }
    }
}