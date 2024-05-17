package com.miempresa.gasapp.adapter

import android.content.Context
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.miempresa.gasapp.R

class WifiListAdapter(context: Context, resource: Int, objects: MutableList<ScanResult>) :
    ArrayAdapter<ScanResult>(context, resource, objects) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.item_wifi, parent, false)
        val scanResult = getItem(position)
        val wifiNameTextView = view.findViewById<TextView>(R.id.tv_wifi_1)
        wifiNameTextView.text = scanResult?.SSID
        return view
    }
}