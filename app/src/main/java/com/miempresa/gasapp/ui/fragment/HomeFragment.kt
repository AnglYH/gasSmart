package com.miempresa.gasapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.miempresa.gasapp.adapter.ScreenSlidePagerAdapter
import com.miempresa.gasapp.databinding.FragmentHomeBinding
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.ui.dialog.PromocionesDialogFragment


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val viewPager = binding.pager
        val tabLayout = binding.tabLayout

        val prefix = "AQ0"
        val sensorList = listOf(
            Sensor(id = 1, code = prefix + "1"),
            Sensor(id = 2, code = prefix + "2"),
            Sensor(id = 3, code = prefix + "3"),
            Sensor(id = 4, code = prefix + "4"),
            Sensor(id = 5, code = prefix + "5"),

        )

        val pagerAdapter = ScreenSlidePagerAdapter(this, sensorList)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // ...
        }.attach()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PromocionesDialogFragment().show(childFragmentManager, "PromocionesDialog")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}