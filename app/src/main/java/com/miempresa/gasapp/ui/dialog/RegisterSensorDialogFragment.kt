package com.miempresa.gasapp.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.miempresa.gasapp.R
import com.miempresa.gasapp.ui.activity.SensorWifiActivity

class RegisterSensorDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val view = inflater.inflate(R.layout.dialog_register_sensor, null)
            builder.setView(view)

            val btnRegisterSensor = view.findViewById<Button>(R.id.btn_register_sensor)
            btnRegisterSensor.setOnClickListener {
                val intent = Intent(context, SensorWifiActivity::class.java)
                startActivity(intent)
            }
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

}