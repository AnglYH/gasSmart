package com.miempresa.gasapp.adapter

import android.content.Context
import android.net.wifi.ScanResult
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.miempresa.gasapp.R

class WifiListAdapter(context: Context, wifiList: List<ScanResult>) : ArrayAdapter<ScanResult>(context, 0, wifiList) {
        // Sobreescribimos el método getView para personalizar la vista de cada elemento de la lista
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // Obtenemos el elemento de la lista en la posición indicada
            val wifi = getItem(position)

            // Creamos una vista a partir del layout personalizado item_wifi
            var view = convertView
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_wifi, parent, false)
            }

            // Obtenemos las referencias a los TextView del layout
            val tvWifiName = view?.findViewById<TextView>(R.id.tv_ssid)
            //val tvWifiCapabilities = view?.findViewById<TextView>(R.id.tv_capabilities)

            // Asignamos los valores correspondientes a los TextView
            tvWifiName?.text = wifi?.SSID
            //tvWifiCapabilities?.text = wifi?.capabilities
            return view!!
        }
}