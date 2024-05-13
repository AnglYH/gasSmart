package com.miempresa.gasapp.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.miempresa.gasapp.R

// Clase principal de la actividad del mapa
class MapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private lateinit var mMap: GoogleMap  // Variable para el objeto GoogleMap

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE =
            0  // Código de solicitud de permisos de ubicación
    }

    // Método que se llama cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)  // Establece la vista de la actividad
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment  // Obtiene el fragmento del mapa
        mapFragment.getMapAsync(this)  // Obtiene el mapa de forma asíncrona
    }

    // Método que se llama cuando el mapa está listo para usarse
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap  // Asigna el mapa a la variable mMap
        mMap.setOnMyLocationButtonClickListener(this)  // Establece el oyente del botón de ubicación
        mMap.setOnMyLocationClickListener(this)  // Establece el oyente del clic en la ubicación
        enableLocation()  // Habilita la ubicación

        // Agrega un marcador en la ubicación de la estación de gas y mueve la cámara
        val distribuidora1 = LatLng(-16.427318, -71.518144)
        val distribuidora2 = LatLng(-16.429016, -71.523862)
        val distribuidora3 = LatLng(-16.425723, -71.527022)
        val distribuidora4 = LatLng(-16.425682, -71.532834)
        val distribuidora5 = LatLng(-16.423721, -71.532067)
        // Agrega más distribuidoras


        mMap.addMarker(MarkerOptions().position(distribuidora1).title("Distribuidora 1"))
        mMap.addMarker(MarkerOptions().position(distribuidora2).title("Distribuidora 2"))
        mMap.addMarker(MarkerOptions().position(distribuidora3).title("Distribuidora 3"))
        mMap.addMarker(MarkerOptions().position(distribuidora4).title("Distribuidora 4"))
        mMap.addMarker(MarkerOptions().position(distribuidora5).title("Distribuidora 5"))
        // Agrega más marcadores

        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(distribuidora1, 15f), 1000, null
        )
    }

    // Método para verificar si los permisos de ubicación están concedidos
    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Método para habilitar la ubicación en el mapa
    private fun enableLocation() {
        if (!::mMap.isInitialized) return
        if (checkLocationPermission()) {
            mMap.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    // Método para solicitar los permisos de ubicación
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            Toast.makeText(this, "Ve a ajuste y aceptas los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Método que se llama cuando el usuario responde a la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        ) // Llama al método de la superclase
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Verifica si los permisos de ubicación están disponibles antes de habilitar la ubicación en el mapa
                    if (checkLocationPermission()) {
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Para activar la localizacion ve a ajustes y acepta los permisos ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            else -> {}
        }
    }

    // Método que se llama cuando se reanudan los fragmentos
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::mMap.isInitialized) return
        if (!checkLocationPermission()) {
            mMap.isMyLocationEnabled = false
            Toast.makeText(
                this,
                "Para activar la localizacion ve a ajustes y acepta los permisos ",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Método que se llama cuando se hace clic en el botón de ubicación
    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Tu Ubicacion", Toast.LENGTH_SHORT).show()
        return false
    }

    // Método que se llama cuando se hace clic en la ubicación
    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estas en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }
}