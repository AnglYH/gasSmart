package com.miempresa.gasapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.miempresa.gasapp.R
import com.miempresa.gasapp.databinding.FragmentHomeSlideItemBinding
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.ui.activity.BluetoothPairingActivity
import com.miempresa.gasapp.ui.viewmodel.SensorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SensorListAdapter(
    private val sensorList: List<Sensor>,
    private val inflater: LayoutInflater,
    private val viewModel: SensorViewModel,
    private val fragment: Fragment
) : RecyclerView.Adapter<SensorListAdapter.SensorViewHolder>() {

    inner class SensorViewHolder(val binding: FragmentHomeSlideItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val binding = FragmentHomeSlideItemBinding.inflate(inflater, parent, false)

        // Hacer el botón invisible por defecto
        binding.btnAddSensor.visibility = View.INVISIBLE
        return SensorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val sensor = sensorList[position]
        holder.binding.tvSensorCode.text = sensor.name

        // Inicia el polling de los datos del sensor
        viewModel.startPollingSensorDataAdapter(sensor.id)

        // Configura el switch
        val sensorSwitch: SwitchMaterial = holder.binding.switchSensor
        sensorSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.mqttHelper.publishMessage("esp32GasSmart/gas_control", isChecked)
        }

        // Observa los datos del sensor y actualiza las vistas cuando cambien
        viewModel.sensorData.observe(fragment.viewLifecycleOwner, Observer { data ->
            val (sensorData, lectura) = data
            if (sensorData?.id == sensor.id) {
                if (sensorData.id == "0") {
                    holder.binding.tvSensorCode.text = sensorData.name
                    holder.binding.imageView.setColorFilter(
                        ContextCompat.getColor(
                            fragment.requireContext(),
                            R.color.silver
                        )
                    )
                    holder.binding.ibtnSensorWifi.setColorFilter(
                        ContextCompat.getColor(
                            fragment.requireContext(),
                            R.color.silver
                        )
                    )
                    holder.binding.switchSensor.visibility = View.GONE
                    holder.binding.tvPercentage.text = ""
                    holder.binding.tvRemainingDays.text = ""
                    holder.binding.tvDate.text = ""
                    holder.binding.btnAddSensor.visibility = View.VISIBLE

                    holder.binding.btnAddSensor.setOnClickListener {
                        val intent = Intent(fragment.context, BluetoothPairingActivity::class.java)
                        fragment.startActivity(intent)
                    }
                } else {
                    holder.binding.tvSensorCode.text = sensorData.name

                    if (!lectura?.fechaLectura.isNullOrEmpty()) {
                        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                        val targetFormat = SimpleDateFormat("dd/MM HH:mm", Locale.US)
                        val date = originalFormat.parse(lectura?.fechaLectura ?: "")
                        val formattedDate = targetFormat.format(date ?: Date())

                        // Calcular los días restantes
                        fragment.viewLifecycleOwner.lifecycleScope.launch {
                            val remainingDays = withContext(Dispatchers.IO) {
                                viewModel.getAverageChangeRate(sensorData.id) // Ajusta el segundo parámetro a tu necesidad
                            }
                            holder.binding.tvRemainingDays.text = "Días restantes: ${remainingDays.toInt()}"
                        }

                        holder.binding.tvDate.text = "Última lectura: $formattedDate"
                        holder.binding.tvPercentage.text = "${lectura?.porcentajeGas}%"
                    } else {
                        holder.binding.tvDate.text = ""
                        holder.binding.tvPercentage.text = ""
                        holder.binding.tvRemainingDays.text = ""
                    }
                }
            }
        })

    }

    override fun getItemCount() = sensorList.size
}