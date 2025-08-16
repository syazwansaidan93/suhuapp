package com.wan.suhu

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class SuhuWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SuhuWorker", "Worker is running to fetch new data.")
        val context = applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, TempAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        try {
            // Try fetching from the primary API
            val primaryRetrofit = Retrofit.Builder()
                .baseUrl("http://suhu.home/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val primaryApiService = primaryRetrofit.create(ApiService::class.java)
            val temperatureData = primaryApiService.getLatestTemperature()

            // Update widget views on success
            views.setTextViewText(R.id.widget_outdoor_temp, String.format(Locale.getDefault(), "outdor : %.1f °C", temperatureData.outdoorTempC))
            views.setTextViewText(R.id.widget_indoor_temp, String.format(Locale.getDefault(), "indoor : %.1f °C", temperatureData.indoorTempC))

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetIds, views)
            Log.d("SuhuWorker", "Successfully fetched and updated widget.")

            return Result.success()

        } catch (e: Exception) {
            // If primary API fails, try the fallback
            Log.e("SuhuWorker", "Primary API failed: ${e.message}. Trying fallback API...")
            try {
                val fallbackRetrofit = Retrofit.Builder()
                    .baseUrl("https://syazwansaidan.site/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val fallbackApiService = fallbackRetrofit.create(ApiService::class.java)
                val temperatureData = fallbackApiService.getLatestTemperature()

                views.setTextViewText(R.id.widget_outdoor_temp, String.format(Locale.getDefault(), "outdor : %.1f °C", temperatureData.outdoorTempC))
                views.setTextViewText(R.id.widget_indoor_temp, String.format(Locale.getDefault(), "indoor : %.1f °C", temperatureData.indoorTempC))

                appWidgetManager.updateAppWidget(appWidgetIds, views)
                Log.d("SuhuWorker", "Successfully fetched from fallback and updated widget.")

                return Result.success()

            } catch (e2: Exception) {
                Log.e("SuhuWorker", "Both primary and fallback APIs failed: ${e2.message}")
                views.setTextViewText(R.id.widget_outdoor_temp, "N/A")
                views.setTextViewText(R.id.widget_indoor_temp, "N/A")

                appWidgetManager.updateAppWidget(appWidgetIds, views)

                return Result.retry()
            }
        }
    }
}
