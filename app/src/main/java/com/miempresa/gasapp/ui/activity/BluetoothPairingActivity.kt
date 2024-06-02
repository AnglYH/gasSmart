package com.miempresa.gasapp.ui.activity

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.miempresa.gasapp.databinding.ActivityBluetoothPairingBinding

class BluetoothPairingActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_DISCOVERABLE_BT = 2
    private val SENSOR_NAME = "ESP32GasSmart" // Reemplaza esto con el nombre de tu sensor

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
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                }
                startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT)
            }
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
            bluetoothAdapter?.bondedDevices?.forEach { device ->
                if (device.name == SENSOR_NAME) {
                    binding.btnVerifyBluetoothPairing.isEnabled = true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bluetoothAdapter?.bondedDevices?.forEach { device ->
            if (device.name == SENSOR_NAME) {
                binding.btnVerifyBluetoothPairing.isEnabled = true
            }
        }
    }
}