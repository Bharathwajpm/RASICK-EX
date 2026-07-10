package com.rasick.shared.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NetworkStatus {
    WIFI, ETHERNET, CELLULAR, OFFLINE
}

class NetworkMonitor(context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _status = MutableStateFlow(getCurrentStatus())
    val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _status.value = getCurrentStatus()
        }

        override fun onLost(network: Network) {
            _status.value = getCurrentStatus()
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            _status.value = getCurrentStatus()
        }
    }

    init {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            // Callback registration fallback
        }
    }

    fun release() {
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            // Ignore unregistration issues
        }
    }

    @Suppress("DEPRECATION")
    private fun getCurrentStatus(): NetworkStatus {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = connectivityManager.activeNetwork ?: return NetworkStatus.OFFLINE
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkStatus.OFFLINE
                return when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkStatus.ETHERNET
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.CELLULAR
                    else -> NetworkStatus.OFFLINE
                }
            } else {
                val activeInfo = connectivityManager.activeNetworkInfo ?: return NetworkStatus.OFFLINE
                return when (activeInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> NetworkStatus.WIFI
                    ConnectivityManager.TYPE_ETHERNET -> NetworkStatus.ETHERNET
                    ConnectivityManager.TYPE_MOBILE -> NetworkStatus.CELLULAR
                    else -> NetworkStatus.OFFLINE
                }
            }
        } catch (e: Exception) {
            return NetworkStatus.OFFLINE
        }
    }
}
