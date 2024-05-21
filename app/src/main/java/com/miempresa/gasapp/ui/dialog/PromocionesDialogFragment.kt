package com.miempresa.gasapp.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.miempresa.gasapp.R
import com.miempresa.gasapp.databinding.DialogPromocionesBinding
import com.miempresa.gasapp.ui.fragment.PromotionFragment

class PromocionesDialogFragment : DialogFragment() {
    private var _binding: DialogPromocionesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPromocionesBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)

        val clickListener = View.OnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, PromotionFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}