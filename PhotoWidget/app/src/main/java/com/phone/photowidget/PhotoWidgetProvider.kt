package com.phone.photowidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast

class PhotoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        // Ensure scheduler is active
        PhotoCycleScheduler.scheduleNextCycle(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_CYCLE_NEXT -> {
                val photos = PhotoManager.getPhotoFileNames(context)
                if (photos.size > 1) {
                    val nextIdx = PhotoManager.cycleToNext(context)
                    updateAllWidgets(context)
                    PhotoCycleScheduler.scheduleNextCycle(context)
                    Toast.makeText(
                        context,
                        "Photo ${nextIdx + 1} of ${photos.size}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (photos.isEmpty()) {
                    val openApp = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(openApp)
                }
            }
            ACTION_CYCLE_PREV -> {
                val photos = PhotoManager.getPhotoFileNames(context)
                if (photos.size > 1) {
                    val prevIdx = PhotoManager.cycleToPrev(context)
                    updateAllWidgets(context)
                    PhotoCycleScheduler.scheduleNextCycle(context)
                    Toast.makeText(
                        context,
                        "Photo ${prevIdx + 1} of ${photos.size}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        PhotoCycleScheduler.scheduleNextCycle(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        PhotoCycleScheduler.cancelSchedule(context)
    }

    companion object {
        const val ACTION_CYCLE_NEXT = "com.phone.photowidget.ACTION_CYCLE_NEXT"
        const val ACTION_CYCLE_PREV = "com.phone.photowidget.ACTION_CYCLE_PREV"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_photo)

                val activePhoto = PhotoManager.getActivePhotoFile(context)
                val shape = PhotoManager.getShape(context)
                val borderStyle = PhotoManager.getBorderStyle(context)

                // Render high-DPI curved rectangle widget bitmap
                val bitmap = PhotoBitmapRenderer.renderWidgetBitmap(
                    context = context,
                    photoFile = activePhoto,
                    width = 720,
                    height = 720,
                    shape = shape,
                    borderStyle = borderStyle
                )

                views.setImageViewBitmap(R.id.iv_photo_widget, bitmap)

                val photos = PhotoManager.getPhotoFileNames(context)
                val pendingIntent: PendingIntent = if (photos.isEmpty()) {
                    // No photos yet: tap opens app to select photos
                    val openAppIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    // Tap immediately cycles to next photo
                    val cycleIntent = Intent(context, PhotoWidgetProvider::class.java).apply {
                        action = ACTION_CYCLE_NEXT
                    }
                    PendingIntent.getBroadcast(
                        context,
                        appWidgetId,
                        cycleIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }

                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                views.setOnClickPendingIntent(R.id.iv_photo_widget, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, PhotoWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
