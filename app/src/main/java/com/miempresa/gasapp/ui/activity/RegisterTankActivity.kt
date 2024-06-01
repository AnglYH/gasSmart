package com.miempresa.gasapp.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.miempresa.gasapp.R
import com.miempresa.gasapp.data.GasTankRepository
import com.miempresa.gasapp.data.PurchaseRepository
import com.miempresa.gasapp.ui.dialog.AyudaRegistroBalonDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterTankActivity : AppCompatActivity() {

    private lateinit var purchaseRepository: PurchaseRepository
    private lateinit var gasTankRepository: GasTankRepository
    private lateinit var tvMarca: TextView
    private lateinit var tvValvula: TextView
    private lateinit var tvPeso: TextView
    private lateinit var btn_tank_register : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_tank)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_register_tank)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializamos los elementos de la vista
        tvMarca = findViewById(R.id.marca)
        tvValvula = findViewById(R.id.tipo_valvula)
        tvPeso = findViewById(R.id.spinner3)

        // Inicializamos los repositorios
        purchaseRepository = PurchaseRepository()
        gasTankRepository = GasTankRepository()

        // Recuperamos los detalles de la compra
        mostrarDetallesCompra()

        val btn_tank_register = findViewById<ImageView>(R.id.iv_tank_register)

        btn_tank_register.setOnClickListener {
            val dialog = AyudaRegistroBalonDialogFragment()
            dialog.show(supportFragmentManager, "dialog_wifi")
        }

    }

    private fun mostrarDetallesCompra() {
        // Utilizamos corrutinas para realizar la consulta en un hilo secundario
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val purchases = purchaseRepository.getAllPurchases()
                purchases.forEach { purchase ->
                    // Recuperamos los detalles del balón de gas usando el gasTankId de la compra
                    val gasTank = gasTankRepository.getGasTank(purchase.gasTankId)
                    // Actualizamos los TextViews con los detalles del balón de gas
                    withContext(Dispatchers.Main) {
                        tvMarca.text = gasTank?.gasBrand
                        tvValvula.text = gasTank?.valveType
                        tvPeso.text = gasTank?.gasWeight.toString()
                    }
                }
            } catch(e:Exception) {
                Log.e("ERROR", e.toString())
            }
        }
    }
}