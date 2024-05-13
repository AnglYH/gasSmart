package com.miempresa.gasapp.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.miempresa.gasapp.R

class AyudaRegistroBalonDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val view = inflater.inflate(R.layout.dialog_registro_balon, null)
            builder.setView(view)
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

}