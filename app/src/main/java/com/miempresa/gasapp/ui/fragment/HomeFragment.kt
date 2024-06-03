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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.ui.dialog.PromocionesDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var userId: String? = null
    private val auth = Firebase.auth
    private val currentUser = auth.currentUser
    private val database = Firebase.database

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        CoroutineScope(Dispatchers.IO).launch {
            userId = obtenerUserId()
            withContext(Dispatchers.Main) {
                obtenerListaSensoresUsuario()
            }
        }
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

    private suspend fun obtenerUserId(): String? {
        val snapshot = Firebase.database.getReference("users").child(currentUser?.email!!.replace(".", ",")).get().await()
        return snapshot.child("id").value as String?
    }

    private fun obtenerListaSensoresUsuario() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val sensorsRef = database.getReference("sensores")
            sensorsRef.get().addOnSuccessListener { dataSnapshot ->
                val sensorList = mutableListOf<Sensor>()
                for (sensorSnapshot in dataSnapshot.children) {
                    val sensor = sensorSnapshot.getValue(Sensor::class.java)
                    if (sensor != null && sensor.user_id == userId) {
                        sensorList.add(sensor)
                    }
                }
                setupViewPager(sensorList)
            }.addOnFailureListener {
                // Manejar el error
            }
        }
    }

    private fun setupViewPager(sensorList: List<Sensor>) {
        val viewPager = binding.pager
        val tabLayout = binding.tabLayout

        try {
            val pagerAdapter = ScreenSlidePagerAdapter(this, sensorList)
            viewPager.adapter = pagerAdapter
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                // ...
            }.attach()
        } catch (e: IllegalStateException) {
            // Maneja el error
        }
    }
}