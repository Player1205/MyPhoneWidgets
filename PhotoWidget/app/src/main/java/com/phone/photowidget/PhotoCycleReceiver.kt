package com.phone.photowidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PhotoCycleReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ALARM_CYCLE = "com.phone.photowidget.ACTION_ALARM_CYCLE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_ALARM_CYCLE) {
            val photos = PhotoManager.getPhotoFileNames(context)
            if (photos.size > 1) {
                PhotoManager.cycleToNext(context)
                PhotoWidgetProvider.updateAllWidgets(context)
            }
            // Always schedule the next alarm in sequence
            PhotoCycleScheduler.scheduleNextCycle(context)
        }
    }
}
