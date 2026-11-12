package com.phone.photowidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import java.io.File

class PhotoPanViewerActivity : AppCompatActivity() {

    private lateinit var ivTouchPan: TouchPanImageView
    private lateinit var tvCounter: TextView
    private var currentIndex = 0
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_pan_viewer)

        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        ivTouchPan = findViewById(R.id.iv_touch_pan)
        tvCounter = findViewById(R.id.tv_viewer_counter)

        currentIndex = PhotoManager.getActiveIndex(this)

        setupButtons()
        loadCurrentPhoto()
    }

    private fun setupButtons() {
        findViewById<TextView>(R.id.btn_viewer_close)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_viewer_prev)?.setOnClickListener {
            val photos = PhotoManager.getPhotoFileNames(this)
            if (photos.size > 1) {
                currentIndex = if (currentIndex - 1 < 0) photos.size - 1 else currentIndex - 1
                PhotoManager.setActiveIndex(this, currentIndex)
                loadCurrentPhoto()
                PhotoWidgetProvider.updateAllWidgets(this)
            }
        }

        findViewById<Button>(R.id.btn_viewer_next)?.setOnClickListener {
            val photos = PhotoManager.getPhotoFileNames(this)
            if (photos.size > 1) {
                currentIndex = (currentIndex + 1) % photos.size
                PhotoManager.setActiveIndex(this, currentIndex)
                loadCurrentPhoto()
                PhotoWidgetProvider.updateAllWidgets(this)
            }
        }

        findViewById<Button>(R.id.btn_save_pan_focus)?.setOnClickListener {
            val photos = PhotoManager.getPhotoFileNames(this)
            val currentFileName = photos.getOrNull(currentIndex)
            if (currentFileName != null) {
                val (panX, panY) = ivTouchPan.getNormalizedCenterOffset()
                PhotoManager.setPhotoPanOffset(this, currentFileName, panX, panY)
                PhotoWidgetProvider.updateAllWidgets(this)
                Toast.makeText(this, "Saved focus position for widget!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_viewer_open_options)?.setOnClickListener {
            val intent = Intent(this, WidgetOptionsActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun loadCurrentPhoto() {
        val photos = PhotoManager.getAllPhotoFiles(this)
        if (photos.isEmpty()) {
            Toast.makeText(this, "No photos in rotation. Add photos first!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (currentIndex !in photos.indices) {
            currentIndex = 0
        }

        tvCounter.text = "PHOTO ${currentIndex + 1} OF ${photos.size}"
        val photoFile = photos[currentIndex]
        val bitmap = decodeSampledBitmap(photoFile.absolutePath, 2048, 2048)
        ivTouchPan.setImageBitmap(bitmap)
    }

    private fun decodeSampledBitmap(filePath: String, reqW: Int, reqH: Int): Bitmap? {
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(filePath, opts)

            var inSampleSize = 1
            if (opts.outHeight > reqH || opts.outWidth > reqW) {
                val halfH = opts.outHeight / 2
                val halfW = opts.outWidth / 2
                while ((halfH / inSampleSize) >= reqH && (halfW / inSampleSize) >= reqW) {
                    inSampleSize *= 2
                }
            }

            val decodeOpts = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = BitmapFactory.decodeFile(filePath, decodeOpts) ?: return null

            // EXIF upright orientation check
            val exif = ExifInterface(filePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val degrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (degrees != 0f) {
                val matrix = Matrix().apply { postRotate(degrees) }
                val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                if (rotated != decoded) decoded.recycle()
                rotated
            } else {
                decoded
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
