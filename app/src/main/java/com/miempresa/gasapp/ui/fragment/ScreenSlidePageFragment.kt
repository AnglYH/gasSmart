package com.miempresa.gasapp.ui.fragment
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.miempresa.gasapp.databinding.FragmentHomeSlideItemBinding
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.ui.viewmodel.SensorViewModel
import androidx.lifecycle.Observer
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.ui.activity.BluetoothPairingActivity
import kotlin.math.log

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
    }

    override fun onResume() {
        super.onResume()
        // Inicia el polling de los datos del sensor
        sensor?.let {
            viewModel.startPollingSensorData(it.id)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeSlideItemBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.sensorData.observe(viewLifecycleOwner, Observer { data ->
            val (sensor, lectura) = data
            binding.tvSensorCode.text = sensor?.name
            binding.tvDate.text = lectura?.fecha_lectura
            binding.tvPercentage.text = lectura?.porcentaje_gas.toString()

        })

        binding.btnAddSensor.setOnClickListener {
            val intent = Intent(context, BluetoothPairingActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}