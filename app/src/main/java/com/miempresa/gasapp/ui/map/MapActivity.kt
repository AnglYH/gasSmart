package com.miempresa.gasapp.ui.map

import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.miempresa.gasapp.R
import com.miempresa.gasapp.data.DistributorRepository
import com.miempresa.gasapp.utils.LocationUtils
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)
        LocationUtils.enableLocationOnMap(this, mMap)

        val repository = DistributorRepository()

        lifecycleScope.launch {
            val distributorsFromDb = repository.getAllDistributors()
            val locationsFromDb = distributorsFromDb.map {
                val locationParts = it.location?.split(",") ?: listOf("0", "0")
                LatLng(locationParts[0].toDouble(), locationParts[1].toDouble())
            }
            val titlesFromDb = distributorsFromDb.map { it.name ?: "" }
            LocationUtils.addMarkersToMap(mMap, locationsFromDb, titlesFromDb)
            if (locationsFromDb.isNotEmpty()) {
                LocationUtils.animateCameraToLocation(mMap, locationsFromDb[0], 15f)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        LocationUtils.handlePermissionResult(requestCode, permissions, grantResults, this, mMap)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::mMap.isInitialized) return
        if (!LocationUtils.checkLocationPermission(this)) {
            mMap.isMyLocationEnabled = false
            Toast.makeText(this, "Para activar la localizacion ve a ajustes y acepta los permisos ", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Tu Ubicacion", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estas en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }
}