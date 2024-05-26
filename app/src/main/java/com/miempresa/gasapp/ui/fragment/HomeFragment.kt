package com.miempresa.gasapp.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.miempresa.gasapp.DAO.UserDao
import com.miempresa.gasapp.adapter.ScreenSlidePagerAdapter
import com.miempresa.gasapp.databinding.FragmentHomeBinding
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.network.ApiClient
import com.miempresa.gasapp.network.ApiService
import com.miempresa.gasapp.ui.dialog.PromocionesDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var userDao: UserDao
    private val apiService: ApiService by lazy {
        ApiClient.getClient().create(ApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val viewPager = binding.pager
        val tabLayout = binding.tabLayout

        //val idUsuario = obtenerIdUsuarioActual()
        CoroutineScope(Dispatchers.IO).launch {
            val sensorList = obtenerListaSensores()
            //val usuarioActual = userDao.getUserById(obtenerIdUsuarioActual())

            withContext(Dispatchers.Main) {
                val pagerAdapter = ScreenSlidePagerAdapter(this@HomeFragment, sensorList)
                viewPager.adapter = pagerAdapter

                    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                        // ...
                    }.attach()
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

    private suspend fun obtenerListaSensores():List<Sensor> {
        val response = apiService.obtenerSensores()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            // Maneja el error
            return emptyList()
        }
    }

    /*private suspend fun obtenerSensoresPorUsuario(idUsuario: String): List<Sensor> {
        val response = apiService.obtenerSensoresPorUsuario(idUsuario)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            // Maneja el error
            return emptyList()
        }
    }

    private fun obtenerIdUsuarioActual(): Int {
        val sharedPreferences = context?.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        return sharedPreferences?.getInt("idUsuario", 0) ?: 0
    }*/

}