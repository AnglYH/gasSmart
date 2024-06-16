package com.miempresa.gasapp.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.miempresa.gasapp.databinding.FragmentHomeBinding
import com.miempresa.gasapp.model.Sensor
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.miempresa.gasapp.adapter.SensorListAdapter
import com.miempresa.gasapp.data.SensorRepository
import com.miempresa.gasapp.ui.dialog.PromocionesDialogFragment
import com.miempresa.gasapp.ui.viewmodel.SensorViewModel
import com.miempresa.gasapp.ui.viewmodel.SensorViewModelFactory
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
    private lateinit var viewModel: SensorViewModel
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var snapHelper: PagerSnapHelper

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(firstVisibleItemPosition))
        }
    }
    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            val smoothScroller = CustomLinearSmoothScroller(context!!)
            smoothScroller.targetPosition = tab.position
            layoutManager.startSmoothScroll(smoothScroller)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}

        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        snapHelper = PagerSnapHelper()

        // ...
        val sensorRepository = SensorRepository()
        val viewModelFactory = SensorViewModelFactory(requireActivity().application, sensorRepository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SensorViewModel::class.java)

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
            sensorsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val sensorList = mutableListOf<Sensor>()
                    for (sensorSnapshot in dataSnapshot.children) {
                        val sensor = sensorSnapshot.getValue(Sensor::class.java)
                        sensor?.let {
                            if (it.userId == userId) {
                                sensorList.add(it)
                            }
                        }
                    }
                    viewModel.startPollingSensorDataHome(sensorList.map { it.id })
                    sensorList.add(Sensor(id = "0", name = "Agregar sensor", userId = ""))

                    // Check if _binding is null before using it
                    _binding?.let { binding ->
                        setupRecyclerView(sensorList, binding)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("HomeFragment", "Error al obtener la lista de sensores")
                }
            })
        }
    }

    private fun setupRecyclerView(sensorList: List<Sensor>, binding: FragmentHomeBinding) {
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = SensorListAdapter(sensorList, layoutInflater, viewModel, this)

        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.removeOnScrollListener(scrollListener)
        recyclerView.addOnScrollListener(scrollListener)

        binding.tabLayout.clearOnTabSelectedListeners()
        binding.tabLayout.addOnTabSelectedListener(tabSelectedListener)

        binding.tabLayout.removeAllTabs()
        for (i in 0 until sensorList.size) {
            binding.tabLayout.addTab(binding.tabLayout.newTab())
        }
    }

    class CustomLinearSmoothScroller(context: Context) : LinearSmoothScroller(context) {
        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
            return 2f / displayMetrics.densityDpi
        }
    }
}