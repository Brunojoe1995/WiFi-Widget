package com.w2sv.data.model

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.data.R
import com.w2sv.data.networking.IPAddress
import com.w2sv.data.networking.connectivityManager
import com.w2sv.data.networking.frequencyToChannel
import com.w2sv.data.networking.getIPAddresses
import com.w2sv.data.networking.wifiManager

@Suppress("DEPRECATION")
sealed class WifiProperty(
    val viewData: ViewData,
    val getValue: (ValueGetterResources) -> Value,
    override val defaultValue: Boolean = true
) :
    DataStoreEntry.UniType<Boolean> {

    sealed class IPProperty(
        viewData: ViewData,
        getValue: (ValueGetterResources) -> Value,
        val prefixLengthSubProperty: SubProperty
    ) : WifiProperty(viewData, getValue) {

        open val subProperties: List<SubProperty> = listOf(prefixLengthSubProperty)

        class SubProperty(
            @StringRes val labelRes: Int,
            override val preferencesKey: Preferences.Key<Boolean>,
            override val defaultValue: Boolean = true
        ) : DataStoreEntry.UniType<Boolean>

        companion object {
            fun values(): Array<IPProperty> {
                return arrayOf(
                    IPv4,
                    IPv6
                )
            }
        }
    }

    data class ViewData(
        @StringRes val labelRes: Int,
        @StringRes val descriptionRes: Int,
        val learnMoreUrl: String?
    )

    sealed interface Value {
        class Singular(val value: String) : Value
        class IPAddresses(val property: IPProperty, val addresses: List<IPAddress>) : Value

        companion object {
            fun getForIPProperty(
                property: IPProperty,
                ipAddresses: List<IPAddress>?,
                type: IPAddress.Type
            ): Value =
                ipAddresses
                    ?.filter { it.type == type }
                    ?.let { addresses ->
                        IPAddresses(property, addresses)
                    }
                    ?: Singular(type.fallbackAddress)
        }
    }

    class ValueGetterResources(val context: Context) {
        val wifiManager by lazy {
            context.wifiManager
        }

        val ipAddresses: List<IPAddress>? by lazy {
            context.connectivityManager.getIPAddresses()
        }
    }

    override val preferencesKey: Preferences.Key<Boolean> =
        booleanPreferencesKey(this::class.simpleName!!)  // TODO

    object SSID : WifiProperty(
        ViewData(
            R.string.ssid,
            R.string.ssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#SSID"
        ),
        {
            Value.Singular(
                it.wifiManager.connectionInfo.ssid?.replace("\"", "") ?: DEFAULT_FALLBACK_VALUE
            )
        },
        defaultValue = false
    )

    object BSSID : WifiProperty(
        ViewData(
            R.string.bssid,
            R.string.bssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#BSSID"
        ),
        {
            Value.Singular(
                it.wifiManager.connectionInfo.bssid ?: DEFAULT_FALLBACK_VALUE
            )
        },
        defaultValue = false
    )

    object IPv4 :
        IPProperty(
            ViewData(
                R.string.ipv4,
                R.string.ipv4_description,
                "https://en.wikipedia.org/wiki/IP_address"
            ),
            {
                Value.getForIPProperty(
                    IPv4,
                    it.ipAddresses,
                    IPAddress.Type.V4
                )
            },
            SubProperty(
                R.string.prefix_length,
                booleanPreferencesKey("IPv4.PrefixLength")
            )
        )

    object IPv6 :
        IPProperty(
            ViewData(
                R.string.ipv6,
                R.string.ipv6_description,
                "https://en.wikipedia.org/wiki/IP_address"
            ),
            {
                Value.getForIPProperty(
                    property = IPv6,
                    ipAddresses = it.ipAddresses,
                    type = IPAddress.Type.V6
                )
            },
            SubProperty(
                R.string.prefix_length,
                booleanPreferencesKey("IPv6.PrefixLength")
            )
        ) {
        val includeLocal =
            SubProperty(R.string.include_local, booleanPreferencesKey("IPv6.IncludeLocal"))
        val includePublic =
            SubProperty(R.string.include_public, booleanPreferencesKey("IPv6.IncludePublic"))

        override val subProperties: List<SubProperty> = listOf(
            includeLocal, includePublic, prefixLengthSubProperty
        )
    }

    object Frequency : WifiProperty(
        ViewData(
            R.string.frequency,
            R.string.frequency_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels"
        ),
        { Value.Singular("${it.wifiManager.connectionInfo.frequency} MHz") },
    )

    object Channel : WifiProperty(
        ViewData(
            R.string.channel,
            R.string.channel_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels"
        ),
        { Value.Singular(frequencyToChannel(it.wifiManager.connectionInfo.frequency).toString()) },
    )

    object LinkSpeed : WifiProperty(
        ViewData(
            R.string.link_speed,
            R.string.link_speed_description,
            null
        ),
        { Value.Singular("${it.wifiManager.connectionInfo.linkSpeed} Mbps") },
    )

    object Gateway : WifiProperty(
        ViewData(
            R.string.gateway,
            R.string.gateway_description,
            "https://en.wikipedia.org/wiki/Gateway_(telecommunications)#Network_gateway"
        ),
        {
            Value.Singular(
                com.w2sv.data.networking.textualIPv4Representation(it.wifiManager.dhcpInfo.gateway)
                    ?: IPAddress.Type.V4.fallbackAddress
            )
        },
    )

    object DNS : WifiProperty(
        ViewData(
            R.string.dns,
            R.string.dns_description,
            "https://en.wikipedia.org/wiki/Domain_Name_System"
        ),
        {
            Value.Singular(
                com.w2sv.data.networking.textualIPv4Representation(it.wifiManager.dhcpInfo.dns1)
                    ?: IPAddress.Type.V4.fallbackAddress
            )
        },
    )

    object DHCP : WifiProperty(
        ViewData(
            R.string.dhcp,
            R.string.dhcp_description,
            "https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol"
        ),
        {
            Value.Singular(
                com.w2sv.data.networking.textualIPv4Representation(it.wifiManager.dhcpInfo.serverAddress)
                    ?: IPAddress.Type.V4.fallbackAddress
            )
        },
    )

    companion object {
        fun values(): Array<WifiProperty> {
            return arrayOf(
                SSID,
                BSSID,
                IPv4,
                IPv6,
                Frequency,
                Channel,
                LinkSpeed,
                Gateway,
                DNS,
                DHCP
            )
        }

        fun valueOf(value: String): WifiProperty {
            return when (value) {
                "SSID" -> SSID
                "BSSID" -> BSSID
                "IPv4" -> IPv4
                "IPv6" -> IPv6
                "Frequency" -> Frequency
                "Channel" -> Channel
                "LinkSpeed" -> LinkSpeed
                "Gateway" -> Gateway
                "DNS" -> DNS
                "DHCP" -> DHCP
                else -> throw IllegalArgumentException("No object com.w2sv.data.model.WifiProperty.$value")
            }
        }
    }
}

private const val DEFAULT_FALLBACK_VALUE = "Couldn't retrieve"