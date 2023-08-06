package com.w2sv.data.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager

val Context.wifiManager: WifiManager get() = getSystemService(WifiManager::class.java)

val Context.connectivityManager: ConnectivityManager get() = getSystemService(ConnectivityManager::class.java)

val ConnectivityManager.linkProperties: LinkProperties? get() = getLinkProperties(activeNetwork)

/**
 * activeNetwork: null when there is no default network, or when the default network is blocked.
 * getNetworkCapabilities: null if the network is unknown or if the |network| argument is null.
 *
 * Reference: https://stackoverflow.com/questions/3841317/how-do-i-see-if-wi-fi-is-connected-on-android
 */
val ConnectivityManager.isWifiConnected: Boolean?
    get() =
        getNetworkCapabilities(activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
