import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.view.View
import com.miempresa.gasapp.R
import com.miempresa.gasapp.databinding.ActivitySensorWifiBinding

class WifiStateReceiver(private val binding: ActivitySensorWifiBinding) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)

        when (wifiStateExtra) {
            WifiManager.WIFI_STATE_ENABLED -> {
                binding.ivToggleWifi.setImageResource(R.drawable.ic_wifi_on_24dp)
                binding.lvWifi.visibility = View.VISIBLE // Muestra el ListView cuando el Wi-Fi está encendido

            }
            WifiManager.WIFI_STATE_DISABLED -> {
                binding.ivToggleWifi.setImageResource(R.drawable.ic_wifi_off_24dp)
                binding.lvWifi.visibility = View.INVISIBLE // Oculta el ListView cuando el Wi-Fi está apagado

            }
        }
    }
}