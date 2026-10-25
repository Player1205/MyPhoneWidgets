package com.antiprocrastination.daysleft

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate
import java.util.Locale

class DaysLeftWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val quote = QuoteManager.getCurrentQuote(context)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, quote)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_CYCLE_QUOTE) {
            val newQuote = QuoteManager.cycleToNextQuote(context)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, DaysLeftWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, newQuote)
            }
        }
    }

    companion object {
        const val ACTION_CYCLE_QUOTE = "com.antiprocrastination.daysleft.ACTION_CYCLE_QUOTE"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            quote: String
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_days_left)

            // Date & Countdown Calculations
            val today = LocalDate.now()
            val year = today.year
            val totalDays = if (today.isLeapYear) 366 else 365
            val dayOfYear = today.dayOfYear
            val daysLeft = totalDays - dayOfYear
            val percentGone = (dayOfYear.toDouble() / totalDays.toDouble()) * 100.0

            // Apply Data to UI
            views.setTextViewText(R.id.tv_year_label, "YEAR $year")
            views.setTextViewText(R.id.tv_days_number, daysLeft.toString())
            views.setTextViewText(
                R.id.tv_percent_badge,
                String.format(Locale.US, "%.1f%% GONE", percentGone)
            )
            views.setProgressBar(R.id.progress_year, 100, percentGone.toInt(), false)
            views.setTextViewText(R.id.tv_quote_text, quote)

            // Setup Click PendingIntent to cycle quote on tap
            val cycleIntent = Intent(context, DaysLeftWidgetProvider::class.java).apply {
                action = ACTION_CYCLE_QUOTE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                cycleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.btn_refresh, pendingIntent)
            views.setOnClickPendingIntent(R.id.quote_container, pendingIntent)
            views.setOnClickPendingIntent(R.id.tv_days_number, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
