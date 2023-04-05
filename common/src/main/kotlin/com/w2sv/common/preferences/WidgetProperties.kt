package com.w2sv.common.preferences

import android.content.SharedPreferences
import com.w2sv.androidutils.typedpreferences.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetProperties @Inject constructor(sharedPreferences: SharedPreferences) :
    BooleanPreferences(
        "SSID" to false,
        "IP" to true,
        "Frequency" to true,
        "Link Speed" to true,
        "Gateway" to true,
        "Netmask" to true,
        "DNS" to true,
        "DHCP" to true,
        sharedPreferences = sharedPreferences
    ) {
    var SSID by this
}