package com.miempresa.gasapp.ui.fragment
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.miempresa.gasapp.databinding.FragmentHomeSlideItemBinding
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.ui.viewmodel.SensorViewModel
import androidx.lifecycle.Observer
import com.miempresa.gasapp.R
import com.miempresa.gasapp.ui.activity.BluetoothPairingActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ScreenSlidePageFragment es responsable de mostrar los detalles de un sensor.
 * Observa los datos del sensor y actualiza la interfaz de usuario en consecuencia.
 */
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

        // Hacer el botón invisible por defecto
        binding.btnAddSensor.visibility = View.INVISIBLE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sensorData.observe(viewLifecycleOwner, Observer { data ->
            val (sensor, lectura) = data
            Log.d("Hola", "Prueba")
            if (sensor?.id == "0") {
                Log.d("ScreenSlidePageFragment", "No hay sensores registrados")
                binding.tvSensorCode.text = sensor.name
                binding.imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.silver))
                binding.ibtnSensorWifi.setColorFilter(ContextCompat.getColor(requireContext(), R.color.silver))
                binding.tvPercentage.text = ""
                binding.tvRemainingDays.text = ""
                binding.tvDate.text = ""
                binding.btnAddSensor.visibility = View.VISIBLE
                // Habilita el botón para registrar un sensor
            } else {
                // Si no es el sensor ficticio, muestra los detalles del sensor
                Log.d("ScreenSlidePageFragment", "Mostrando datos del sensor") // Nuevo mensaje de registro
                binding.tvSensorCode.text = sensor?.name

                if (!lectura?.fechaLectura.isNullOrEmpty()) {
                    // Parsea la fecha y la formatea
                    val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    val targetFormat = SimpleDateFormat("dd/MM HH:mm", Locale.US)
                    val date = originalFormat.parse(lectura?.fechaLectura ?: "")
                    val formattedDate = targetFormat.format(date ?: Date())

                    binding.tvDate.text = "Última lectura: $formattedDate"
                    binding.tvPercentage.text = "${lectura?.porcentajeGas}%"
                } else {
                    // Si no hay lecturas, establece los TextViews a un estado predeterminado
                    binding.tvDate.text = ""
                    binding.tvPercentage.text = ""
                }
            }
        })

        binding.btnAddSensor.setOnClickListener {
            val intent = Intent(context, BluetoothPairingActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}