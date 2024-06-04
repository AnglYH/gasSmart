package com.miempresa.gasapp.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.miempresa.gasapp.R
import com.miempresa.gasapp.data.DistributorRepository
import com.miempresa.gasapp.databinding.FragmentStoreBinding
import com.miempresa.gasapp.model.ValveType
import com.miempresa.gasapp.ui.map.MapActivity
import com.miempresa.gasapp.utils.LocationUtils
import kotlinx.coroutines.launch
import java.util.Locale
import com.google.firebase.database.FirebaseDatabase
import com.miempresa.gasapp.model.Purchase
import java.time.LocalDate
import java.util.UUID


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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        return root
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
                            val address = it[0]
                            val addressLine = address.getAddressLine(0)
                            val district = address.subLocality // Aquí obtenemos el distrito
                            binding.etAddress.setText("$addressLine, $district")
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
        val repository = DistributorRepository()

        lifecycleScope.launch {
            val distributorsFromDb = repository.getAllDistributors()
            val distributorNames = distributorsFromDb.map { it.name }

            // Configurar el ArrayAdapter para el AutoCompleteTextView
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ArrayList(distributorNames))

            // Configurar el AutoCompleteTextView con el ArrayAdapter
            val distributorView = view.findViewById<AutoCompleteTextView>(R.id.et_distributor)
            distributorView.setAdapter(adapter)

            // Deshabilitar la entrada de texto
            distributorView.inputType = InputType.TYPE_NULL

            // Configurar el OnItemClickListener para el AutoCompleteTextView de los distribuidores
            distributorView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                // Recuperar el distribuidor seleccionado
                val selectedDistributor = distributorsFromDb[position]
                // Recuperar las marcas asociadas al distribuidor seleccionado
                val brands = selectedDistributor.marca?.filterNotNull()?.map { it.nombre } ?: emptyList()

                // Imprimir las marcas en la terminal
                Log.d("StoreFragment", "Marcas asociadas al distribuidor seleccionado: $brands")

                // Configurar el ArrayAdapter para el AutoCompleteTextView de las marcas
                val brandAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ArrayList(brands))
                val brandView = view.findViewById<AutoCompleteTextView>(R.id.et_tank_brand)
                brandView.setAdapter(brandAdapter)

                // Deshabilitar la entrada de texto
                brandView.inputType = InputType.TYPE_NULL

                // Forzar a que el desplegable se muestre
                brandView.requestFocus()
                brandView.showDropDown()

                // Configurar el OnItemClickListener para el AutoCompleteTextView de las marcas
                brandView.onItemClickListener = AdapterView.OnItemClickListener { _, _, brandPosition, _ ->
                    // Recuperar la marca seleccionada
                    val selectedBrand = selectedDistributor.marca?.filterNotNull()?.get(brandPosition)

                    // Recuperar los tipos de válvulas asociados a la marca seleccionada
                    val valves = selectedBrand?.valvula?.keys?.toList() ?: emptyList()

                    // Configurar el ArrayAdapter para el AutoCompleteTextView de los tipos de válvulas
                    val valveAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ArrayList(valves))
                    val valveView = view.findViewById<AutoCompleteTextView>(R.id.et_valve)
                    valveView.setAdapter(valveAdapter)

                    // Deshabilitar la entrada de texto
                    valveView.inputType = InputType.TYPE_NULL

                    // Forzar a que el desplegable se muestre
                    valveView.requestFocus()
                    valveView.showDropDown()

                    // Configurar el OnItemClickListener para el AutoCompleteTextView de los tipos de válvulas
                    valveView.onItemClickListener = AdapterView.OnItemClickListener { _, _, valvePosition, _ ->
                        // Recuperar el tipo de válvula seleccionado
                        val selectedValveType = valves[valvePosition]

                        // Imprimir el tipo de válvula en la terminal
                        Log.d("StoreFragment", "Tipo de válvula seleccionado: $selectedValveType")

                        // Recuperar los datos de la válvula seleccionada
                        val selectedValveData = selectedBrand?.valvula?.get(selectedValveType) as? ValveType

                        // Recuperar el peso asociado al tipo de válvula seleccionado
                        val weight = selectedValveData?.peso

                        // Imprimir el peso en la terminal
                        Log.d("StoreFragment", "Peso asociado al tipo de válvula seleccionado: $weight")

                        // Configurar el ArrayAdapter para el AutoCompleteTextView del peso
                        val weightAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf(weight ?: ""))
                        val weightView = view.findViewById<AutoCompleteTextView>(R.id.et_tank_weight)
                        weightView.setAdapter(weightAdapter)

                        // Deshabilitar la entrada de texto
                        weightView.inputType = InputType.TYPE_NULL

                        // Forzar a que el desplegable se muestre
                        weightView.showDropDown()

                        // Recuperar el precio asociado al tipo de válvula seleccionado
                        val price = selectedValveData?.precio

                        // Imprimir el precio en la terminal
                        Log.d("StoreFragment", "Precio asociado al tipo de válvula seleccionado: $price")

                        // Configurar el TextView del total con el precio
                        val totalTextView = view.findViewById<TextView>(R.id.tv_total)
                        totalTextView.text = "Total: S/ ${price ?: 0.00}"
                    }
                }
            }

            // Mostrar la lista desplegable cuando se haga clic en el AutoCompleteTextView
            distributorView.setOnClickListener {
                distributorView.showDropDown()
            }
        }
        val btnAddTank = view.findViewById<Button>(R.id.btn_add_tank)
        btnAddTank.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar compra")
                .setMessage("¿Estás seguro de que quieres realizar esta compra?")
                .setPositiveButton("Sí") { _, _ ->
                }
                .setNegativeButton("No", null)
                .show()
        }
    }




    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setOnMapClickListener {
            val intent = Intent(activity, MapActivity::class.java)
            startActivity(intent)
        }
        LocationUtils.enableLocationOnMap(requireActivity(), googleMap)

        val repository = DistributorRepository()

        // Recuperar las distribuidoras de la base de datos y agregar marcadores al mapa
        lifecycleScope.launch {
            val distribuidorasFromDb = repository.getAllDistributors()
            val locationsFromDb = distribuidorasFromDb.mapNotNull {
                if (it.latitud != null && it.longitud != null) LatLng(it.latitud, it.longitud) else null
            }
            val titlesFromDb = distribuidorasFromDb.mapNotNull { it.name }
            LocationUtils.addMarkersToMap(googleMap, locationsFromDb, titlesFromDb.filterNotNull())
            if (locationsFromDb.isNotEmpty()) {
                LocationUtils.animateCameraToLocation(googleMap, locationsFromDb[0], 15f)
            }
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