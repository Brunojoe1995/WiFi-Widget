package com.w2sv.networking.model

import com.w2sv.common.utils.log
import com.w2sv.domain.model.IpLocationParameter
import com.w2sv.networking.extensions.fetchFromUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import slimber.log.i
import java.io.IOException

/**
 * Data fetched from json retrieved from [ip-api](http://ip-api.com/json?fields=5828601).
 * [Field documentation](https://ip-api.com/docs/api:json)
 *
 * @property status `success` or `fail`
 * @property message fail reason, if any
 */
@Serializable
internal data class IpApiData(
    private val status: String = "",
    private val message: String = "",
    private val continent: String = "",
    private val country: String = "",
    private val regionName: String = "",
    private val city: String = "",
    private val district: String = "",
    private val zip: String = "",
    private val lat: Double = -1.0,
    private val lon: Double = -1.0,
    val timezone: String = "",
    val isp: String = "",
    @SerialName("as") private val asNumberAndOrganization: String = "",
//    @SerialName("asname") val asName: String = ""
) {
    fun location(parameters: Map<IpLocationParameter, Boolean>): String? {
        val parameterToField = mapOf(
            IpLocationParameter.ZipCode to zip,
            IpLocationParameter.District to district,
            IpLocationParameter.City to city,
            IpLocationParameter.Region to regionName,
            IpLocationParameter.Country to country,
            IpLocationParameter.Continent to continent
        )
        return parameters
            .map { (parameter, isEnabled) -> if (isEnabled) parameterToField.getValue(parameter) else null }
            .filter { !it.isNullOrBlank() }
            .joinToString(", ")
    }

    val gpsCoordinates: String? by lazy {
        if (lat == -1.0 || lon == -1.0) null else "$lat, $lon"
    }

    val asn: String? by lazy {
        asNumberAndOrganization.run {
            if (isBlank())
                null
            else
                substringBefore(" ")
        }
    }

    companion object {
        suspend fun fetch(client: OkHttpClient): Result<IpApiData> =
            client.fetchFromUrl("http://ip-api.com/json?fields=5828601") { jsonString ->
                i { "Fetched json string: $jsonString" }
                json
                    .decodeFromString<IpApiData>(jsonString)
                    .log { "Fetched $it" }
                    .also { if (it.status == "fail") throw IOException("Fetching IpApiData failed with message ${it.message}") }
            }
    }
}

private val json = Json { ignoreUnknownKeys = true }
