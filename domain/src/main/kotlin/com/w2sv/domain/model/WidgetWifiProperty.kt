package com.w2sv.domain.model

import androidx.annotation.StringRes
import com.w2sv.domain.R

sealed class WidgetWifiProperty(
    val viewData: ViewData,
    val defaultIsEnabled: Boolean,
) {
    data class ViewData(
        @StringRes val labelRes: Int,
        @StringRes val descriptionRes: Int,
        val learnMoreUrl: String? = null,
    )

    sealed interface Value {
        @JvmInline
        value class String(val value: kotlin.String) : Value
        @JvmInline
        value class IPAddresses(val addresses: List<IPAddress>) : Value
    }

    interface ValueGetter {
        operator fun invoke(properties: List<WidgetWifiProperty>): List<Value?>
    }

    data object SSID : WidgetWifiProperty(
        ViewData(
            R.string.ssid,
            R.string.ssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#SSID",
        ),
        true
    )

    data object BSSID : WidgetWifiProperty(
        ViewData(
            R.string.bssid,
            R.string.bssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#BSSID",
        ),
        false
    )

    sealed class IPProperty(
        viewData: ViewData,
        defaultIsEnabled: Boolean,
        val prefixLengthViewData: ViewData?
    ) :
        WidgetWifiProperty(viewData, defaultIsEnabled) {

        sealed class V4AndV6(
            viewData: ViewData,
            defaultIsEnabled: Boolean,
            prefixLengthViewData: ViewData?
        ) : IPProperty(viewData, defaultIsEnabled, prefixLengthViewData)

        sealed class V6Only(
            viewData: ViewData,
            defaultIsEnabled: Boolean,
            prefixLengthViewData: ViewData?
        ) : IPProperty(viewData, defaultIsEnabled, prefixLengthViewData)

        companion object {
            const val LEARN_MORE_URL = "https://en.wikipedia.org/wiki/IP_address"

            val prefixLengthViewData = ViewData(
                R.string.show_prefix_length,
                R.string.prefix_length_description,
                "https://www.ibm.com/docs/en/ts3500-tape-library?topic=formats-subnet-masks-ipv4-prefixes-ipv6",
            )
        }
    }

    data object LinkLocal :
        IPProperty.V4AndV6(
            ViewData(
                R.string.ipv4,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            true,
            prefixLengthViewData
        )

    data object SiteLocal :
        IPProperty.V4AndV6(
            ViewData(
                R.string.ipv4,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            true,
            prefixLengthViewData
        )

    data object UniqueLocal :
        IPProperty.V6Only(
            ViewData(
                R.string.ipv4,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            true,
            prefixLengthViewData
        )

    data object GlobalUnicast :
        IPProperty.V6Only(
            ViewData(
                R.string.ipv4,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            true,
            prefixLengthViewData
        )

    data object Public :
        IPProperty.V4AndV6(
            ViewData(
                R.string.ipv4,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            false,
            prefixLengthViewData
        )

    data object Frequency : WidgetWifiProperty(
        ViewData(
            R.string.frequency,
            R.string.frequency_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels",
        ),
        true
    )

    data object Channel : WidgetWifiProperty(
        ViewData(
            R.string.channel,
            R.string.channel_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels",
        ),
        true
    )

    data object LinkSpeed : WidgetWifiProperty(
        ViewData(
            R.string.link_speed,
            R.string.link_speed_description,
            null,
        ),
        true
    )

    data object Gateway : WidgetWifiProperty(
        ViewData(
            R.string.gateway,
            R.string.gateway_description,
            "https://en.wikipedia.org/wiki/Gateway_(telecommunications)#Network_gateway",
        ),
        true
    )

    data object DNS : WidgetWifiProperty(
        ViewData(
            R.string.dns,
            R.string.dns_description,
            "https://en.wikipedia.org/wiki/Domain_Name_System",
        ),
        true
    )

    data object DHCP : WidgetWifiProperty(
        ViewData(
            R.string.dhcp,
            R.string.dhcp_description,
            "https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol",
        ),
        true
    )

    companion object {
        val entries: List<WidgetWifiProperty>
            get() {
                return listOf(
                    SSID,
                    BSSID,
                    LinkLocal,
                    SiteLocal,
                    UniqueLocal,
                    GlobalUnicast,
                    Public,
                    Frequency,
                    Channel,
                    LinkSpeed,
                    Gateway,
                    DNS,
                    DHCP,
                )
            }
    }
}
