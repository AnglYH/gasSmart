package com.miempresa.gasapp.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

object LocationUtils {
    private const val LOCATION_PERMISSION_REQUEST_CODE = 0

    fun checkLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission(activity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            Toast.makeText(activity, "Ve a ajuste y aceptas los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                activity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun zoomToLocation(googleMap: GoogleMap, latitude: Double, longitude: Double, zoomLevel: Float) {
        val latLng = LatLng(latitude, longitude)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
    }

    fun enableLocationOnMap(activity: Activity, googleMap: GoogleMap) {
        if (checkLocationPermission(activity)) {
            googleMap.isMyLocationEnabled = true
        } else {
            requestLocationPermission(activity)
        }
    }

    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        activity: Activity,
        googleMap: GoogleMap
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkLocationPermission(activity)) {
                        googleMap.isMyLocationEnabled = true
                    }
                } else {
                    Toast.makeText(
                        activity,
                        "Para activar la localizacion ve a ajustes y acepta los permisos ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun addMarkersToMap(googleMap: GoogleMap, locations: List<LatLng>, titles: List<String>) {
        for (i in locations.indices) {
            googleMap.addMarker(MarkerOptions().position(locations[i]).title(titles[i]))
        }
    }

    fun animateCameraToLocation(googleMap: GoogleMap, location: LatLng, zoomLevel: Float) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
    }
}