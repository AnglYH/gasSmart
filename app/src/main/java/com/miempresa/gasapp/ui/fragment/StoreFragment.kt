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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.miempresa.gasapp.R
import com.miempresa.gasapp.data.DistributorRepository
import com.miempresa.gasapp.databinding.FragmentStoreBinding
import com.miempresa.gasapp.model.ValveType
import com.miempresa.gasapp.ui.map.MapActivity
import com.miempresa.gasapp.utils.LocationUtils
import kotlinx.coroutines.launch
import java.util.Locale
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.api.MessageBody
import com.miempresa.gasapp.api.RetrofitClient
import com.miempresa.gasapp.model.Purchase
import com.miempresa.gasapp.ui.viewmodel.SensorViewModel
import retrofit2.Call
import retrofit2.Response
import java.time.LocalDate
import java.util.UUID
import com.miempresa.gasapp.BuildConfig
import com.miempresa.gasapp.data.SensorManager
import com.miempresa.gasapp.data.SensorRepository
import com.miempresa.gasapp.ui.viewmodel.SensorViewModelFactory

class StoreFragment : Fragment(), OnMapReadyCallback {
    // Declarar la variable en el alcance de la clase
    private var distributorPhone: String? = null
    private var distribuidorId: String? = null

    private lateinit var sensorViewModel: SensorViewModel

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var paymentMethod: String? = null

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

        binding.tvStoreTitle.visibility = View.GONE

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sensorRepository = SensorRepository()
        val sensorViewModelFactory = SensorViewModelFactory(requireActivity().application, sensorRepository)
        sensorViewModel = ViewModelProvider(this, sensorViewModelFactory).get(SensorViewModel::class.java)

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

                            // Cambia el tipo de letra del EditText
                            val typeface = ResourcesCompat.getFont(requireContext(), R.font.quicksand_regular)
                            binding.etAddress.typeface = typeface
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
            paymentMethod = "Yape"
        }

        plinButton.setOnClickListener {
            yapeButton.setImageResource(R.mipmap.icon_yape_round)
            plinButton.setImageResource(R.mipmap.icon_plin_round)
            efectivoButton.setImageResource(R.mipmap.icon_cash_round)
            paymentTextView.text = "Pagar con: Plin"
            paymentMethod = "Plin"
        }

        efectivoButton.setOnClickListener {
            yapeButton.setImageResource(R.mipmap.icon_yape_round)
            plinButton.setImageResource(R.mipmap.icon_plin_round)
            efectivoButton.setImageResource(R.mipmap.icon_cash_round)
            paymentTextView.text = "Pagar con: Efectivo"
            paymentMethod = "Efectivo"
        }


        // Recuperar las distribuidoras de la base de datos
        val repository = DistributorRepository()

        lifecycleScope.launch {
            val distributorsFromDb = repository.getAllDistributors()
            val marcasFromDb = distributorsFromDb.mapNotNull { it.marca?.mapNotNull { it.nombre } }.flatten()
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

                distribuidorId = selectedDistributor.id

                // Imprimir las marcas en la terminal
                Log.d("StoreFragment", "Marcas asociadas al distribuidor seleccionado: $brands")

                // Asignar el número de teléfono del distribuidor seleccionado a la variable
                distributorPhone = distributorsFromDb[position].phone

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

                    // Obtener el id de la marca
                    val brandId = selectedBrand?.id

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
            lifecycleScope.launch {
                val user_id = Firebase.auth.currentUser?.uid
                val sensor_id = user_id?.let { sensorViewModel.getSensorIdByUserId(it) }

                Log.d("StoreFragment", "ID del usuario: $user_id")
                Log.d("StoreFragment", "ID del sensor: $sensor_id")

                AlertDialog.Builder(requireContext())
                    .setTitle("Confirmar compra")
                    .setMessage("¿Estás seguro de que quieres realizar esta compra?")
                    .setPositiveButton("Sí") { _, _ ->
                        val date = LocalDate.now().toString()
                        val direccion = view.findViewById<EditText>(R.id.et_address).text.toString()
                        //val distributor_Id = distribuidorId
                        val id = UUID.randomUUID().toString()
                        val marca_id = view.findViewById<AutoCompleteTextView>(R.id.et_tank_brand).text.toString()
                        val peso_balon = view.findViewById<AutoCompleteTextView>(R.id.et_tank_weight).text.toString()
                        val price = view.findViewById<TextView>(R.id.tv_total).text.toString().removePrefix("Total: S/ ")
                        val valvula_balon = view.findViewById<AutoCompleteTextView>(R.id.et_valve).text.toString()

                        Log.d("StoreFragment", "Fecha: $date")
                        Log.d("StoreFragment", "Dirección: $direccion")
                        //Log.d("StoreFragment", "ID del distribuidor: $distributor_Id")
                        Log.d("StoreFragment", "ID de la compra: $id")
                        Log.d("StoreFragment", "ID de la marca: $marca_id")
                        Log.d("StoreFragment", "Peso del balón: $peso_balon")
                        Log.d("StoreFragment", "Precio: $price")
                        Log.d("StoreFragment", "ID de la válvula: $valvula_balon")

                        val purchase = Purchase(date, direccion, distribuidorId.toString(), id, marca_id, peso_balon, price, sensor_id, user_id, valvula_balon)

                        val database = FirebaseDatabase.getInstance()
                        val reference = database.getReference("compras")
                        reference.child(id).setValue(purchase)

                        // Obtén el ID de la compra que acabas de agregar a la base de datos
                        val newPurchaseId = id

                        // Instanciar SensorManager
                        val sensorManager = SensorManager()

                        // Actualizar el ID de la compra en la colección de sensores
                        sensor_id?.let { sensorId ->
                            sensorManager.updateSensorWithPurchase(sensorId, newPurchaseId)

                            // Mostrar en un log el ID que se está insertando
                            Log.d("StoreFragment", "Insertando ID de compra en la base de datos: $newPurchaseId")
                        }



                        // Enviar mensaje de WhatsApp
                        val userPhone = Firebase.auth.currentUser?.phoneNumber
                        val message = "Nuevo pedido:\n" +
                                "Fecha: $date\n" +
                                "Dirección de entrega: $direccion\n" +
                                "Marca del balón: $marca_id\n" +
                                "Peso del balón: $peso_balon\n" +
                                "Tipo de válvula: $valvula_balon\n" +
                                "Precio: $price\n" +
                                "Método de pago: $paymentMethod\n" +
                                "Por favor, confirme la recepción de este pedido."
                        userPhone?.let {
                            distributorPhone?.let { phone ->
                                sendWhatsAppMessage(BuildConfig.GREEN_INSTANCE_ID, BuildConfig.GREEN_API_TOKEN, phone, message)
                            }
                        }
                        // Limpiar todos los campos

                        view.findViewById<AutoCompleteTextView>(R.id.et_distributor).setText("")
                        view.findViewById<AutoCompleteTextView>(R.id.et_tank_brand).setText("")
                        view.findViewById<AutoCompleteTextView>(R.id.et_tank_weight).setText("")
                        view.findViewById<AutoCompleteTextView>(R.id.et_valve).setText("")
                        view.findViewById<TextView>(R.id.tv_total).text = "Total: S/ 0.00"
                        view.findViewById<TextView>(R.id.textPay).text = "Pagar con:"
                        yapeButton.setImageResource(R.mipmap.icon_yape_round)
                        plinButton.setImageResource(R.mipmap.icon_plin_round)
                        efectivoButton.setImageResource(R.mipmap.icon_cash_round)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }

    private fun sendWhatsAppMessage(idInstance: String, apiTokenInstance: String, receiverPhoneNumber: String, message: String) {
        Log.d("GreenAPI", "sendWhatsAppMessage called with idInstance: $idInstance, apiTokenInstance: $apiTokenInstance, receiverPhoneNumber: $receiverPhoneNumber, message: $message")

        val chatId = "$receiverPhoneNumber@c.us"
        val messageBody = MessageBody(chatId, message)

        val call = RetrofitClient.greenApiService.sendMessage(idInstance, apiTokenInstance, messageBody)
        Log.d("GreenAPI", "API call created: $call")
        call.enqueue(object : retrofit2.Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    Log.d("GreenAPI", "Message sent successfully: ${response.body()}")
                } else {
                    Log.e("GreenAPI", "Failed to send message: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.e("GreenAPI", "Error: ${t.message}")
            }
        })
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