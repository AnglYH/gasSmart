package com.miempresa.gasapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.miempresa.gasapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_notifications,
                R.id.navigation_store, R.id.navigation_profile
            )
        )

        val sharedPref = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        val hasShownDialog = sharedPref.getBoolean("hasShownDialog", false)

        if (!hasShownDialog) {
            showNotificationDialog()
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun showNotificationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notificaciones")
            .setMessage("¿Deseas recibir notificaciones de la aplicación?")
            .setPositiveButton("Sí") { _, _ ->
                enableNotifications()
                saveDialogShown()
            }
            .setNegativeButton("No") { _, _ ->
                saveDialogShown()
            }
            .show()
    }

    private fun enableNotifications() {
        val intent = Intent().apply {
            action = "android.settings.APP_NOTIFICATION_SETTINGS"

            // for Android 5-7
            putExtra("app_package", packageName)
            putExtra("app_uid", applicationInfo.uid)

            // for Android 8 and above
            putExtra("android.provider.extra.APP_PACKAGE", packageName)
        }

        startActivity(intent)
    }

    private fun saveDialogShown() {
        val sharedPref = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("hasShownDialog", true)
            apply()
        }
    }
}