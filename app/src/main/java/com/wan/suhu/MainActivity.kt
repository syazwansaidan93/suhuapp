package com.wan.suhu

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // UI elements using lazy initialization for robustness
    private val indoorTempText: TextView by lazy { findViewById(R.id.indoor_temp_text) }
    private val outdoorTempText: TextView by lazy { findViewById(R.id.outdoor_temp_text) }
    private val apiStatusIndicator: android.view.View by lazy { findViewById(R.id.api_status_indicator) }
    private val apiStatusText: TextView by lazy { findViewById(R.id.api_status_text) }

    // Retrofit service and coroutines
    private lateinit var primaryApiService: ApiService
    private lateinit var fallbackApiService: ApiService
    private var fetchJob: Job? = null

    // Handler for periodic data fetching
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1500L // 1.5 seconds

    private val fetchRunnable = object : Runnable {
        override fun run() {
            fetchTemperatureData()
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Retrofit for the primary API
        val primaryRetrofit = Retrofit.Builder()
            .baseUrl("http://suhu.home/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        primaryApiService = primaryRetrofit.create(ApiService::class.java)

        // Initialize Retrofit for the fallback API
        val fallbackRetrofit = Retrofit.Builder()
            .baseUrl("https://syazwansaidan.site/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        fallbackApiService = fallbackRetrofit.create(ApiService::class.java)
    }

    override fun onResume() {
        super.onResume()
        // Start fetching data when the app comes to the foreground
        startFetchingData()
    }

    override fun onPause() {
        super.onPause()
        // Stop fetching data when the app goes to the background
        stopFetchingData()
    }

    /**
     * Starts the periodic fetching of temperature data.
     */
    private fun startFetchingData() {
        // Start the first fetch immediately
        fetchRunnable.run()
    }

    /**
     * Stops the periodic fetching of temperature data.
     */
    private fun stopFetchingData() {
        // Remove any pending callbacks from the handler
        handler.removeCallbacks(fetchRunnable)
        // Cancel the coroutine job if it's running
        fetchJob?.cancel()
    }

    /**
     * Fetches temperature data from the primary API and falls back to a secondary API if needed.
     */
    private fun fetchTemperatureData() {
        fetchJob?.cancel() // Cancel any previous job to avoid overlapping requests
        fetchJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // Try fetching from the primary API
                val temperatureData = primaryApiService.getLatestTemperature()
                updateUI(temperatureData)
                Log.d("MainActivity", "Successfully fetched data from primary API")
            } catch (e: Exception) {
                // If primary API fails, try the fallback
                Log.e("MainActivity", "Primary API failed: ${e.message}. Trying fallback API...")
                try {
                    val temperatureData = fallbackApiService.getLatestTemperature()
                    updateUI(temperatureData)
                    Log.d("MainActivity", "Successfully fetched data from fallback API")
                } catch (e2: Exception) {
                    // If both APIs fail, show error state in UI
                    Log.e("MainActivity", "Both primary and fallback APIs failed: ${e2.message}")
                    runOnUiThread {
                        indoorTempText.text = getString(R.string.not_available)
                        outdoorTempText.text = getString(R.string.not_available)
                        apiStatusIndicator.setBackgroundResource(R.drawable.status_indicator_offline)
                        apiStatusText.text = getString(R.string.offline)
                    }
                }
            }
        }
    }

    /**
     * Updates the UI with the fetched temperature data.
     */
    private fun updateUI(temperatureData: TemperatureData) {
        runOnUiThread {
            indoorTempText.text = String.format(Locale.getDefault(), "%.1f °C", temperatureData.indoorTempC)
            outdoorTempText.text = String.format(Locale.getDefault(), "%.1f °C", temperatureData.outdoorTempC)
            apiStatusIndicator.setBackgroundResource(R.drawable.status_indicator_online)
            apiStatusText.text = getString(R.string.online)
        }
    }
}
