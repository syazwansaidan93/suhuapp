package com.wan.suhu

import retrofit2.http.GET

/**
 * Retrofit API service interface for fetching temperature data.
 */
interface ApiService {

    /**
     * Defines a GET request to the "t/latest" endpoint.
     * The response will be automatically converted into a TemperatureData object.
     */
    @GET("t/latest")
    suspend fun getLatestTemperature(): TemperatureData
}
