package com.miempresa.gasapp.ui.fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.miempresa.gasapp.databinding.FragmentHomeSlideItemBinding
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.network.ApiClient
import com.miempresa.gasapp.network.ApiService
import com.miempresa.gasapp.ui.dialog.RegisterSensorDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenSlidePageFragment : Fragment() {
    private var sensor: Sensor? = null
    private val sensorList = mutableListOf<Sensor>()
    private var _binding: FragmentHomeSlideItemBinding? = null
    private val binding get() = _binding!!

    private val apiService: ApiService by lazy {
        ApiClient.getClient().create(ApiService::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensor = arguments?.getSerializable("sensor") as Sensor?    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeSlideItemBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Usa el ID del sensor que este fragmento representa
        val idSensor = sensor?.idSensor.toString()
        CoroutineScope(Dispatchers.IO).launch {
            val responseLecturas = apiService.obtenerLecturasPorSensor(idSensor)
            val responseSensores = apiService.obtenerSensores()

            if (responseLecturas.isSuccessful && responseSensores.isSuccessful) {
                val lecturaList = responseLecturas.body()
                val sensorList = responseSensores.body()

                // Asegúrate de que la lista de lecturas y sensores no estén vacías
                if (!lecturaList.isNullOrEmpty() && !sensorList.isNullOrEmpty()) {
                    // Ordena las lecturas por fecha en orden descendente y selecciona la primera
                    val lectura = lecturaList.maxByOrNull { it.fecha }!!

                    // Busca el sensor con el idSensor correspondiente
                    val sensor = sensorList.find { it.idSensor == lectura.idSensor }

                    // Actualiza tvSensorCode en el hilo principal
                    withContext(Dispatchers.Main) {
                        binding.tvSensorCode.text = "Sensor ${sensor?.code}"
                        binding.tvRemainingDays.text = "Días restantes: ${lectura.diasRest}"
                        binding.tvDate.text = "Fecha: ${lectura.fecha}"
                        binding.tvPercentage.text = "Porcentaje: ${lectura.porcentaje}%"
                    }
                }
            } else {
                // Maneja el error
            }
        }

        /*sensor?.let {
            binding.tvSensorCode.text =  "Sensor ${it.code}"
        } ?: run {
            // Handle the case where sensor is null

        }*/

        binding.btnAddSensor.setOnClickListener {
            /*// Crea un nuevo sensor
            val newSensor = Sensor(
                idSensor = sensorList.size + 1, // Asigna un nuevo ID al sensor
                code = "Sensor ${sensorList.size + 1}", // Asigna un nuevo código al sensor
                idBalon = sensorList.size + 1 // Asigna un nuevo ID de balón al sensor
            )

            // Añade el nuevo sensor a la lista
            sensorList.add(newSensor)*/

            // Muestra el diálogo de registro de sensor
            val dialog = RegisterSensorDialogFragment()
            dialog.show(parentFragmentManager, "RegisterSensorDialogFragment")
        }

        // Button to access sensor wifi configuration
        //binding.ibtnSensorWifi.setOnClickListener {
        //    val intent = Intent(context, SensorWifiActivity::class.java)
        //    startActivity(intent)
        //}

        return root
    }
}