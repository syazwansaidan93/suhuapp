package com.wan.suhu

import com.google.gson.annotations.SerializedName

/**
 * A data class to represent the temperature data received from the API.
 * The @SerializedName annotation maps the JSON keys to the Kotlin properties.
 */
data class TemperatureData(
    @SerializedName("indoor_temp_C")
    val indoorTempC: Double,

    @SerializedName("outdoor_temp_C")
    val outdoorTempC: Double
)
