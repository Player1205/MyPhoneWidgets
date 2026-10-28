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

                // Dynamically match launcher widget aspect ratio (eliminates vertical/horizontal compression)
                val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)?.takeIf { it > 0 } ?: 150
                val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)?.takeIf { it > 0 } ?: 180

                val targetWidth = 720
                val targetHeight = (720f * minHeight / minWidth).toInt().coerceIn(600, 1100)

                // Render high-DPI curved rectangle widget bitmap
                val bitmap = PhotoBitmapRenderer.renderWidgetBitmap(
                    context = context,
                    photoFile = activePhoto,
                    width = targetWidth,
                    height = targetHeight,
                    shape = shape,
                    borderStyle = borderStyle
                )

                views.setImageViewBitmap(R.id.iv_photo_widget, bitmap)

                // Tap or tap-and-hold opens the Quick Options dialog (Set Time, Add Photos, Next Photo)
                val openOptionsIntent = Intent(context, WidgetOptionsActivity::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    openOptionsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

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
