package com.phone.photowidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

object PhotoManager {

    private const val PREFS_NAME = "photo_widget_prefs"
    private const val KEY_PHOTOS = "saved_photo_filenames"
    private const val KEY_ACTIVE_INDEX = "active_photo_index"
    private const val KEY_INTERVAL_MINUTES = "interval_minutes"
    private const val KEY_LAST_CYCLE_TIME = "last_cycle_timestamp"
    private const val KEY_SHAPE = "widget_shape"
    private const val KEY_BORDER_STYLE = "border_style"

    const val SHAPE_ROUNDED = "rounded"
    const val SHAPE_OVAL = "oval"
    const val SHAPE_CIRCLE = "circle"

    const val BORDER_DARK = "dark_rim"
    const val BORDER_AMBER = "amber_rim"
    const val BORDER_NONE = "none"

    const val DEFAULT_INTERVAL_MINUTES = 15

    private fun getPhotosDir(context: Context): File {
        val dir = File(context.filesDir, "widget_photos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun getPhotoFileNames(context: Context): List<String> {
        val json = getPrefs(context).getString(KEY_PHOTOS, null) ?: return emptyList()
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val name = arr.getString(i)
                val file = File(getPhotosDir(context), name)
                if (file.exists()) {
                    list.add(name)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    @Synchronized
    private fun savePhotoFileNames(context: Context, names: List<String>) {
        val arr = JSONArray()
        for (name in names) {
            arr.put(name)
        }
        getPrefs(context).edit().putString(KEY_PHOTOS, arr.toString()).apply()
    }

    @Synchronized
    fun addPhotosFromUris(context: Context, uris: List<Uri>): Int {
        val existingNames = getPhotoFileNames(context).toMutableList()
        val dir = getPhotosDir(context)
        var addedCount = 0

        val baseTime = System.currentTimeMillis()
        for ((idx, uri) in uris.withIndex()) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val decoded = decodeSampledBitmapFromStream(inputStream, 1600, 1600)
                    inputStream.close()

                    if (decoded != null) {
                        val fileName = "photo_${baseTime}_${idx}.jpg"
                        val targetFile = File(dir, fileName)
                        val out = FileOutputStream(targetFile)
                        decoded.compress(Bitmap.CompressFormat.JPEG, 92, out)
                        out.flush()
                        out.close()
                        decoded.recycle()

                        existingNames.add(fileName)
                        addedCount++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (addedCount > 0) {
            savePhotoFileNames(context, existingNames)
            if (getActiveIndex(context) >= existingNames.size || getActiveIndex(context) < 0) {
                setActiveIndex(context, 0)
            }
        }
        return addedCount
    }

    @Synchronized
    fun deletePhoto(context: Context, index: Int): Boolean {
        val list = getPhotoFileNames(context).toMutableList()
        if (index !in list.indices) return false

        val fileName = list.removeAt(index)
        val file = File(getPhotosDir(context), fileName)
        if (file.exists()) {
            file.delete()
        }
        savePhotoFileNames(context, list)

        val currentActive = getActiveIndex(context)
        when {
            list.isEmpty() -> setActiveIndex(context, 0)
            currentActive >= list.size -> setActiveIndex(context, list.size - 1)
            index < currentActive -> setActiveIndex(context, currentActive - 1)
        }
        return true
    }

    @Synchronized
    fun getActiveIndex(context: Context): Int {
        val list = getPhotoFileNames(context)
        if (list.isEmpty()) return 0
        val idx = getPrefs(context).getInt(KEY_ACTIVE_INDEX, 0)
        return if (idx in list.indices) idx else 0
    }

    @Synchronized
    fun setActiveIndex(context: Context, index: Int) {
        val list = getPhotoFileNames(context)
        val safeIndex = if (list.isNotEmpty() && index in list.indices) index else 0
        getPrefs(context).edit()
            .putInt(KEY_ACTIVE_INDEX, safeIndex)
            .putLong(KEY_LAST_CYCLE_TIME, System.currentTimeMillis())
            .apply()
    }

    @Synchronized
    fun cycleToNext(context: Context): Int {
        val list = getPhotoFileNames(context)
        if (list.isEmpty()) return 0
        val current = getActiveIndex(context)
        val next = (current + 1) % list.size
        setActiveIndex(context, next)
        return next
    }

    @Synchronized
    fun cycleToPrev(context: Context): Int {
        val list = getPhotoFileNames(context)
        if (list.isEmpty()) return 0
        val current = getActiveIndex(context)
        val prev = if (current - 1 < 0) list.size - 1 else current - 1
        setActiveIndex(context, prev)
        return prev
    }

    fun getActivePhotoFile(context: Context): File? {
        val list = getPhotoFileNames(context)
        if (list.isEmpty()) return null
        val idx = getActiveIndex(context)
        val name = list.getOrNull(idx) ?: return null
        val file = File(getPhotosDir(context), name)
        return if (file.exists()) file else null
    }

    fun getAllPhotoFiles(context: Context): List<File> {
        val names = getPhotoFileNames(context)
        val dir = getPhotosDir(context)
        return names.mapNotNull { name ->
            val f = File(dir, name)
            if (f.exists()) f else null
        }
    }

    fun getIntervalMinutes(context: Context): Int {
        return getPrefs(context).getInt(KEY_INTERVAL_MINUTES, DEFAULT_INTERVAL_MINUTES)
    }

    fun setIntervalMinutes(context: Context, minutes: Int) {
        getPrefs(context).edit()
            .putInt(KEY_INTERVAL_MINUTES, minutes)
            .putLong(KEY_LAST_CYCLE_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getLastCycleTimestamp(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_CYCLE_TIME, System.currentTimeMillis())
    }

    fun getRemainingMinutes(context: Context): Int {
        val interval = getIntervalMinutes(context)
        val lastCycle = getLastCycleTimestamp(context)
        val elapsed = (System.currentTimeMillis() - lastCycle) / (60 * 1000L)
        val remaining = interval - elapsed
        return max(1L, remaining).toInt()
    }

    fun getShape(context: Context): String {
        return getPrefs(context).getString(KEY_SHAPE, SHAPE_ROUNDED) ?: SHAPE_ROUNDED
    }

    fun setShape(context: Context, shape: String) {
        getPrefs(context).edit().putString(KEY_SHAPE, shape).apply()
    }

    fun getBorderStyle(context: Context): String {
        return getPrefs(context).getString(KEY_BORDER_STYLE, BORDER_DARK) ?: BORDER_DARK
    }

    fun setBorderStyle(context: Context, borderStyle: String) {
        getPrefs(context).edit().putString(KEY_BORDER_STYLE, borderStyle).apply()
    }

    private fun decodeSampledBitmapFromStream(
        inputStream: InputStream,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        val bytes = inputStream.readBytes()
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        var inSampleSize = 1
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            val halfHeight: Int = options.outHeight / 2
            val halfWidth: Int = options.outWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
    }
}
