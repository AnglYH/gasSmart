package com.miempresa.gasapp.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.miempresa.gasapp.databinding.ActivityBluetoothPairingBinding

class BluetoothPairingActivity : AppCompatActivity() {

    lateinit var binding: ActivityBluetoothPairingBinding
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

        binding.btnVerifyBluetoothPairing.setOnClickListener {
            val intentWifiActivity = Intent(this, SensorWifiActivity::class.java)
            startActivity(intentWifiActivity)
        }
    }
}