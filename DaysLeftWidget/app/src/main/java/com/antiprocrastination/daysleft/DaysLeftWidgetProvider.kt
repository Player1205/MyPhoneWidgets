package com.antiprocrastination.daysleft

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate

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
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_days_left)

                // Date & Countdown Calculations
                val today = LocalDate.now()
                val year = today.year
                val totalDays = if (today.isLeapYear) 366 else 365
                val dayOfYear = today.dayOfYear
                val daysLeft = totalDays - dayOfYear

                // Apply Data to UI
                views.setTextViewText(R.id.tv_year_label, "in $year")
                views.setTextViewText(R.id.tv_days_hero, "$daysLeft days left")
                views.setTextViewText(R.id.tv_quote_label, "QUOTE BY ME:")
                views.setTextViewText(R.id.tv_quote_text, "“$quote”")

                // Generate and apply dynamic 365 Year Dots background
                val dotsBitmap = YearDotsGenerator.generateYearDotsBitmap(today = today)
                views.setImageViewBitmap(R.id.iv_year_dots, dotsBitmap)

                // Open the floating translucent Add Quote dialog directly on the home screen
                val openDialogIntent = Intent(context, AddQuoteActivity::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    openDialogIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                views.setOnClickPendingIntent(R.id.widget_card, pendingIntent)
                views.setOnClickPendingIntent(R.id.tv_days_hero, pendingIntent)
                views.setOnClickPendingIntent(R.id.quote_container, pendingIntent)
                views.setOnClickPendingIntent(R.id.btn_quick_add, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}