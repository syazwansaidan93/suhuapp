package com.wan.suhu

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class TempAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.wan.suhu.ACTION_REFRESH_WIDGET"
        const val WORK_TAG = "SuhuWidgetWorker"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Schedule the worker to run periodically
        val refreshWorkRequest = PeriodicWorkRequest.Builder(SuhuWorker::class.java, 1, TimeUnit.HOURS)
            .addTag(WORK_TAG)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            refreshWorkRequest
        )

        // Set up the click listener for the widget to trigger an immediate refresh
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        val refreshIntent = Intent(context, TempAppWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        // Update the widget with the views, which now contain the click listener
        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ACTION_REFRESH_WIDGET) {
            Log.d("TempAppWidgetProvider", "Widget click detected. Enqueuing new work.")
            if (context != null) {
                // Enqueue an immediate one-time work request to refresh data
                val workRequest = OneTimeWorkRequest.Builder(SuhuWorker::class.java)
                    .addTag(WORK_TAG)
                    .build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first instance of the widget is added.
        // We'll immediately trigger a data fetch so the widget isn't empty.
        Log.d("TempAppWidgetProvider", "onEnabled called. Enqueuing initial one-time work.")
        val workRequest = OneTimeWorkRequest.Builder(SuhuWorker::class.java)
            .addTag(WORK_TAG)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    override fun onDisabled(context: Context) {
        // Called when the last instance of the widget is removed.
        // We should cancel all work requests to save resources.
        Log.d("TempAppWidgetProvider", "onDisabled called. Cancelling all work.")
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }
}
