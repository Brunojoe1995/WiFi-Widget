package com.w2sv.networking.model

import com.w2sv.common.utils.log
import com.w2sv.networking.extensions.fetchFromUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import slimber.log.i

/**
 * Data parsed from json retrieved from [ifconfig](https://ifconfig.co/json).
 *
 * Complete json fetched from url may look like this:
 *```
 * {
 *   "ip": ,
 *   "ip_decimal": ,
 *   "country": "Germany",
 *   "country_iso": "DE",
 *   "country_eu": true,
 *   "region_name": ,
 *   "region_code": ,
 *   "zip_code": ,
 *   "city": ,
 *   "latitude": ,
 *   "longitude": ,
 *   "time_zone": "Europe/Berlin",
 *   "asn": "AS3209",
 *   "asn_org": "Vodafone GmbH",
 *   "hostname": "dslb-088-066-169-011.088.066.pools.vodafone-ip.de",
 *   "user_agent": {
 *     "product": "Mozilla",
 *     "version": "5.0",
 *     "comment": "(X11; Ubuntu; Linux x86_64; rv:131.0) Gecko/20100101 Firefox/131.0",
 *     "raw_value": "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:131.0) Gecko/20100101 Firefox/131.0"
 *   }
 * }
 * ```
 */
@Serializable
internal data class IFConfigData(
//    val ip: String,
    val country: String,
    @SerialName("region_name") val regionName: String? = null,
    @SerialName("zip_code") val zipCode: String? = null,
    val city: String? = null,
    val latitude: Double,
    val longitude: Double,
    val asn: String,
    @SerialName("asn_org") val asnOrg: String
) {
    val location: String get() = when {
        listOf(regionName, zipCode, city).any { it == null } -> country
        else -> "$zipCode $city, $regionName, $country"
    }
    val gpsLocation: String get() = "$latitude, $longitude"

    companion object {
        suspend fun fetch(client: OkHttpClient): Result<IFConfigData> =
            client.fetchFromUrl("https://ifconfig.co/json") { jsonString ->
                i { "Fetched json string: $jsonString" }
                json.decodeFromString<IFConfigData>(jsonString).log { "Fetched $it" }
            }
    }
}

private val json = Json { ignoreUnknownKeys = true }
