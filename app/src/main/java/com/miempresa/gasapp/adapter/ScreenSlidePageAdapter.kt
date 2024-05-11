package com.miempresa.gasapp.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.miempresa.gasapp.ui.fragment.ScreenSlidePageFragment
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.ui.fragment.HomeFragment

class ScreenSlidePagerAdapter(fa: HomeFragment, private val sensorList: List<Sensor>) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = sensorList.size

    override fun createFragment(position: Int): Fragment {
        return ScreenSlidePageFragment(sensorList[position])
    }
    //ScreenSlidePageFragment()
}