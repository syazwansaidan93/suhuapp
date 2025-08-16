package com.wan.suhu

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.wan.suhu.R

class TempAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.wan.suhu.ACTION_REFRESH_WIDGET"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ACTION_REFRESH_WIDGET) {
            if (context != null) {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisAppWidget = intent.component
                val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Set up the click listener for the entire widget
        val intent = Intent(context, TempAppWidgetProvider::class.java)
        intent.action = ACTION_REFRESH_WIDGET
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        // Start a coroutine to fetch data for the widget
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Try fetching from the primary API
                val primaryRetrofit = Retrofit.Builder()
                    .baseUrl("http://suhu.home/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val primaryApiService = primaryRetrofit.create(ApiService::class.java)
                val temperatureData = primaryApiService.getLatestTemperature()

                // Update widget views on success
                views.setTextViewText(R.id.widget_outdoor_temp, String.format("outdor : %.1f °C", temperatureData.outdoorTempC))
                views.setTextViewText(R.id.widget_indoor_temp, String.format("indoor : %.1f °C", temperatureData.indoorTempC))

            } catch (e: Exception) {
                // If primary API fails, try the fallback
                Log.e("TempAppWidgetProvider", "Primary API failed: ${e.message}. Trying fallback API...")
                try {
                    val fallbackRetrofit = Retrofit.Builder()
                        .baseUrl("https://syazwansaidan.site/api/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    val fallbackApiService = fallbackRetrofit.create(ApiService::class.java)
                    val temperatureData = fallbackApiService.getLatestTemperature()

                    views.setTextViewText(R.id.widget_outdoor_temp, String.format("outdor : %.1f °C", temperatureData.outdoorTempC))
                    views.setTextViewText(R.id.widget_indoor_temp, String.format("indoor : %.1f °C", temperatureData.indoorTempC))

                } catch (e2: Exception) {
                    Log.e("TempAppWidgetProvider", "Both primary and fallback APIs failed: ${e2.message}")
                    views.setTextViewText(R.id.widget_outdoor_temp, "N/A")
                    views.setTextViewText(R.id.widget_indoor_temp, "N/A")
                }
            }

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
