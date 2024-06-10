package com.miempresa.gasapp.ui.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.miempresa.gasapp.databinding.ActivityBluetoothPairingBinding

class BluetoothPairingActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_DISCOVERABLE_BT = 2
    private val SENSOR_NAME = "8BitDo Pro 2" // Reemplaza esto con el nombre de tu sensor

    private val MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 3
    private val MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 4

    lateinit var binding: ActivityBluetoothPairingBinding
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityBluetoothPairingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.bluetoothPairing) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.ibtnBluetooth.setOnClickListener {
            // Abre la configuración de Bluetooth del dispositivo
            val intentOpenBluetoothSettings = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intentOpenBluetoothSettings)
        }

        binding.btnVerifyBluetoothPairing.setOnClickListener {
            val intentWifiActivity = Intent(this, SensorWifiActivity::class.java)
            startActivity(intentWifiActivity)
        }

        binding.btnVerifyBluetoothPairing.isEnabled = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT)
        } else if (requestCode == REQUEST_DISCOVERABLE_BT) {
            val permissionToCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Manifest.permission.BLUETOOTH_CONNECT
            } else {
                Manifest.permission.BLUETOOTH
            }
            if (ContextCompat.checkSelfPermission(this, permissionToCheck)
                == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter?.bondedDevices?.forEach { device ->
                    if (device.name == SENSOR_NAME) {
                        binding.btnVerifyBluetoothPairing.isEnabled = true
                    }
                }
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(permissionToCheck),
                    MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val permissionToCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            Manifest.permission.BLUETOOTH
        }
        if (ContextCompat.checkSelfPermission(this, permissionToCheck)
            == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.bondedDevices?.forEach { device ->
                if (device.name == SENSOR_NAME) {
                    binding.btnVerifyBluetoothPairing.isEnabled = true
                }
            }
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(permissionToCheck),
                MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT)
        }
    }
}