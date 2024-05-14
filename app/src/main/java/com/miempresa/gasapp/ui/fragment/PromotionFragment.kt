package com.miempresa.gasapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.miempresa.gasapp.R
import com.miempresa.gasapp.databinding.FragmentPromotionsBinding

class PromotionFragment : Fragment() {
    private var _binding: FragmentPromotionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPromotionsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val promotionsButton = root.findViewById<Button>(R.id.promotionsButton)
        val rewardsButton = root.findViewById<Button>(R.id.rewardsButton)
        val promotions = root.findViewById<LinearLayout>(R.id.promotions)

        promotionsButton.setOnClickListener {
            promotions.visibility = View.VISIBLE
        }

        rewardsButton.setOnClickListener {
            promotions.visibility = View.GONE
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}