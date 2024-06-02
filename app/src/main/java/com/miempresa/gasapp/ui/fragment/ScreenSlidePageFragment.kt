package com.miempresa.gasapp.ui.fragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.miempresa.gasapp.databinding.FragmentHomeSlideItemBinding
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.ui.viewmodel.SensorViewModel
import androidx.lifecycle.Observer
import com.miempresa.gasapp.BluetoothPairingActivity

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
            val intent = Intent(context, BluetoothPairingActivity::class.java)
            startActivity(intent)
        }

        /*sensor?.let {
            binding.tvSensorCode.text =  "Sensor ${it.code}"
        } ?: run {
            // Handle the case where sensor is null

        }*/

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