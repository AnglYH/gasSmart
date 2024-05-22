package com.miempresa.gasapp.ui.fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.miempresa.gasapp.databinding.FragmentHomeSlideItemBinding
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.ui.dialog.RegisterSensorDialogFragment
import com.miempresa.gasapp.ui.viewmodel.SensorViewModel
import androidx.lifecycle.Observer

@Suppress("DEPRECATION")
class ScreenSlidePageFragment : Fragment() {
    private var sensor: Sensor? = null
    private var _binding: FragmentHomeSlideItemBinding? = null
    private lateinit var viewModel: SensorViewModel

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensor = arguments?.getSerializable("sensor") as Sensor?
        viewModel = ViewModelProvider(this).get(SensorViewModel::class.java)

        // Observa los cambios en idSensor
        viewModel.idSensor.observe(this, Observer { idSensor ->
            // Carga los datos del sensor
            viewModel.loadSensorData(idSensor)
        })
    }

    override fun onResume() {
        super.onResume()
        // Inicia el polling de los datos del sensor
        sensor?.let {
            viewModel.startPollingSensorData(it.idSensor.toString())
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeSlideItemBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observa los cambios en sensorData
        viewModel.sensorData.observe(viewLifecycleOwner, Observer { data ->
            val (sensor, lectura) = data
            binding.tvSensorCode.text = "Sensor ${sensor?.code}"
            binding.tvRemainingDays.text = "Días restantes: ${lectura?.diasRest}"
            binding.tvDate.text = "Fecha: ${lectura?.fecha}"
            binding.tvPercentage.text = "Porcentaje: ${lectura?.porcentaje}%"
        })

        binding.btnAddSensor.setOnClickListener {
            // Muestra el diálogo de registro de sensor
            val dialog = RegisterSensorDialogFragment()
            dialog.show(parentFragmentManager, "RegisterSensorDialogFragment")
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}