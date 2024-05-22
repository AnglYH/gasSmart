package com.miempresa.gasapp.ui.fragment
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.miempresa.gasapp.R
import com.miempresa.gasapp.databinding.FragmentHomeSlideItemBinding
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.ui.activity.RegisterTankActivity
import com.miempresa.gasapp.ui.activity.SensorWifiActivity
import com.miempresa.gasapp.ui.dialog.RegisterSensorDialogFragment

class ScreenSlidePageFragment : Fragment() {
    private var sensor: Sensor? = null
    private var _binding: FragmentHomeSlideItemBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensor = arguments?.getParcelable<Parcelable>("sensor") as Sensor?    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeSlideItemBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sensor?.let {
            binding.tvSensorCode.text =  "Sensor ${it.code}"
        } ?: run {
            // Handle the case where sensor is null

        }

        binding.btnAddSensor.setOnClickListener {
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