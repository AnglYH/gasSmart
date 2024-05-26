package com.miempresa.gasapp.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.miempresa.gasapp.R
import com.miempresa.gasapp.data.DistribuidoraRepository
import com.miempresa.gasapp.databinding.FragmentStoreBinding
import com.miempresa.gasapp.ui.map.MapActivity
import com.miempresa.gasapp.utils.LocationUtils
import kotlinx.coroutines.launch
import java.util.Locale


class StoreFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapPreview)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Consider calling ActivityCompat#requestPermissions here to request the missing permissions.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    addresses?.let {
                        if (it.isNotEmpty()) {
                            val address = it[0].getAddressLine(0)
                            binding.etAddress.setText(address)
                        }
                    }
                }
            }


        val valveTypes = resources.getStringArray(R.array.tipos_valvula_disponibles)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, valveTypes)

        val valveView = view.findViewById<AutoCompleteTextView>(R.id.et_valve)
        valveView.setAdapter(adapter)
        valveView.inputType = InputType.TYPE_NULL
        valveView.setOnClickListener {
            valveView.showDropDown()
        }
        val paymentTextView = view.findViewById<TextView>(R.id.textPay)
        val yapeButton = view.findViewById<ImageButton>(R.id.yape)
        val plinButton = view.findViewById<ImageButton>(R.id.plin)
        val efectivoButton = view.findViewById<ImageButton>(R.id.efectivo)

        yapeButton.setOnClickListener {
            yapeButton.setImageResource(R.mipmap.icon_yape_round)
            plinButton.setImageResource(R.mipmap.icon_plin_round)
            efectivoButton.setImageResource(R.mipmap.icon_cash_round)
            paymentTextView.text = "Pagar con: Yape"
        }

        plinButton.setOnClickListener {
            yapeButton.setImageResource(R.mipmap.icon_yape_round)
            plinButton.setImageResource(R.mipmap.icon_plin_round)
            efectivoButton.setImageResource(R.mipmap.icon_cash_round)
            paymentTextView.text = "Pagar con: Plin"
        }

        efectivoButton.setOnClickListener {
            yapeButton.setImageResource(R.mipmap.icon_yape_round)
            plinButton.setImageResource(R.mipmap.icon_plin_round)
            efectivoButton.setImageResource(R.mipmap.icon_cash_round)
            paymentTextView.text = "Pagar con: Efectivo"
        }


        // Recuperar las distribuidoras de la base de datos
        val repository = DistribuidoraRepository()


        lifecycleScope.launch {
            val distribuidorasFromDb = repository.getAllDistribuidoras()
            val namesFromDb = distribuidorasFromDb.map { it.nombre } // Cambiado de it.name a it.nombre

            // Configurar el ArrayAdapter para el AutoCompleteTextView
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, namesFromDb)

            // Configurar el AutoCompleteTextView con el ArrayAdapter
            val distributorView = view.findViewById<AutoCompleteTextView>(R.id.et_distributor)
            distributorView.setAdapter(adapter)

            // Deshabilitar la entrada de texto
            distributorView.inputType = InputType.TYPE_NULL

            // Mostrar la lista desplegable cuando se presione el AutoCompleteTextView
            distributorView.setOnClickListener {
                distributorView.showDropDown()
            }
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setOnMapClickListener {
            val intent = Intent(activity, MapActivity::class.java)
            startActivity(intent)
        }
        LocationUtils.enableLocationOnMap(requireActivity(), googleMap)

        val repository = DistribuidoraRepository()

        // Recuperar las distribuidoras de la base de datos y agregar marcadores al mapa
        lifecycleScope.launch {
            val distribuidorasFromDb = repository.getAllDistribuidoras()
            val locationsFromDb = distribuidorasFromDb.map { LatLng(it.latitud, it.longitud) }
            val titlesFromDb = distribuidorasFromDb.map { it.nombre } // Cambiado de it.name a it.nombre
            LocationUtils.addMarkersToMap(googleMap, locationsFromDb, titlesFromDb)
            LocationUtils.animateCameraToLocation(googleMap, locationsFromDb[0], 15f)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (::googleMap.isInitialized) {
            LocationUtils.handlePermissionResult(requestCode, permissions, grantResults, requireActivity(), googleMap)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (!LocationUtils.checkLocationPermission(requireContext())) {
            LocationUtils.requestLocationPermission(requireActivity())
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

}