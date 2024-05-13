package com.miempresa.gasapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.miempresa.gasapp.DAO.GasTankDao
import com.miempresa.gasapp.Database.GasTankDatabase
import com.miempresa.gasapp.MainActivity
import com.miempresa.gasapp.R
import com.miempresa.gasapp.model.GasTank
import com.miempresa.gasapp.ui.dialog.AyudaRegistroBalonDialogFragment
import com.miempresa.gasapp.ui.dialog.AyudaWifiDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterTankActivity : AppCompatActivity() {

    private lateinit var db: GasTankDatabase
    private lateinit var gasTankDao: GasTankDao
    private lateinit var spin1: Spinner
    private lateinit var spin2: Spinner
    private lateinit var spin3: Spinner
    private lateinit var btnAddGasTank: Button
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
        btnAddGasTank = findViewById(R.id.btn_add_tank)
        spin1 = findViewById(R.id.spinner)
        spin2 = findViewById(R.id.spinner2)
        spin3 = findViewById(R.id.spinner3)

        // Inicializamos la base de datos
        db = Room.databaseBuilder(
            applicationContext,
            GasTankDatabase::class.java, "gasTank"
        ).fallbackToDestructiveMigration()
            .build()

        // Se obtiene el GasTankDao
        gasTankDao = db.gasTankDao()


        btnAddGasTank.setOnClickListener {

            val marca = spin1.selectedItem.toString().trim()
            val valvula = spin2.selectedItem.toString().trim()
            val peso = spin3.selectedItem.toString()

            val gasTank = GasTank(
                null,
                marca,
                valvula,
                peso
            )
            agregarBalon(gasTank)
            mostrarBalones()
        }

        val btn_tank_register = findViewById<ImageView>(R.id.iv_tank_register)

        btn_tank_register.setOnClickListener {
            val dialog = AyudaRegistroBalonDialogFragment()
            dialog.show(supportFragmentManager, "dialog_wifi")
        }

    }

    private fun mostrarBalones() {
        // Utilizamos corrutinas para realizar la consulta en un hilo secundario
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val gasTanks = gasTankDao.getAll()
                gasTanks.forEach {
                    Log.d("Balón de gas", it.toString())
                }
            } catch(e:Exception) {
                Log.e("ERROR", e.toString())
            }
        }
    }
    private fun agregarBalon(gasTank: GasTank) {
        // Utilizamos corrutinas para realizar la consulta en un hilo secundario
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                gasTankDao.insert(gasTank)
                mostrarBalones()

                val intent = Intent(this@RegisterTankActivity, MainActivity::class.java)
                startActivity(intent)

            } catch(e:Exception) {
                Log.e("ERROR", e.toString())
            }
        }
    }

}