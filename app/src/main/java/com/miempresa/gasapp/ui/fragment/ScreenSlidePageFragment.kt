package com.miempresa.gasapp.ui.fragment
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
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
        binding.btnAddSensor.visibility = View.INVISIBLE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sensorData.observe(viewLifecycleOwner, Observer { data ->
            val (sensor, lectura) = data
            if (sensor?.id == "0") {
                binding.tvSensorCode.text = sensor.name
                binding.imageView.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.silver
                    )
                )
                binding.ibtnSensorWifi.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.silver
                    )
                )
                binding.tvPercentage.text = ""
                binding.tvRemainingDays.text = ""
                binding.tvDate.text = ""
                binding.btnAddSensor.visibility = View.VISIBLE
            } else {
                binding.tvSensorCode.text = sensor?.name

                if (!lectura?.fechaLectura.isNullOrEmpty()) {
                    val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    val targetFormat = SimpleDateFormat("dd/MM HH:mm", Locale.US)
                    val date = originalFormat.parse(lectura?.fechaLectura ?: "")
                    val formattedDate = targetFormat.format(date ?: Date())

                    binding.tvDate.text = "Última lectura: $formattedDate"
                    binding.tvPercentage.text = "${lectura?.porcentajeGas}%"

                    val percentage = lectura?.porcentajeGas?.toIntOrNull()
                    if (sensor != null && percentage != null && percentage <= 0) {
                        sendNotification(sensor, percentage)
                    }
                } else {
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

    private fun sendNotification(sensor: Sensor, gasPercentage: Int) {
        // Si ya se envió una notificación para este sensor, no hagas nada
        if (sensor.notificationSent) return

        val notificationManager = ContextCompat.getSystemService(
            requireContext(),
            NotificationManager::class.java
        ) as NotificationManager

        val notification = NotificationCompat.Builder(requireContext(), "GasApp")
            .setContentTitle("Advertencia de GasApp")
            .setContentText("El sensor ${sensor.name} tiene un $gasPercentage% de gas restante")
            .setSmallIcon(R.drawable.ic_store_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Utiliza el ID del sensor como el ID de la notificación
        val notificationId = sensor.id.hashCode()
        notificationManager.notify(notificationId, notification)

        // Marca que ya se envió una notificación para este sensor
        sensor.notificationSent = true
    }
}