package com.phone.photowidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class WidgetOptionsActivity : AppCompatActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var tvIntervalStatus: TextView
    private lateinit var tvPhotosCount: TextView

    private lateinit var chip15m: TextView
    private lateinit var chip20m: TextView
    private lateinit var chip30m: TextView
    private lateinit var chip60m: TextView
    private lateinit var chip3h: TextView
    private lateinit var chip6h: TextView
    private lateinit var chip24h: TextView

    private val pickMultipleLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val count = PhotoManager.addPhotosFromUris(this, uris)
            updatePhotosCountUI()
            PhotoWidgetProvider.updateAllWidgets(this)
            PhotoCycleScheduler.scheduleNextCycle(this)
            Toast.makeText(this, "Added $count photo(s) to widget!", Toast.LENGTH_SHORT).show()
        }
    }

    private val fallbackPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val uris = mutableListOf<Uri>()
            result.data?.clipData?.let { clip ->
                for (i in 0 until clip.itemCount) {
                    uris.add(clip.getItemAt(i).uri)
                }
            } ?: result.data?.data?.let { uri ->
                uris.add(uri)
            }
            if (uris.isNotEmpty()) {
                val count = PhotoManager.addPhotosFromUris(this, uris)
                updatePhotosCountUI()
                PhotoWidgetProvider.updateAllWidgets(this)
                PhotoCycleScheduler.scheduleNextCycle(this)
                Toast.makeText(this, "Added $count photo(s) to widget!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_options)

        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        initViews()
        setupListeners()
        updateIntervalChipsUI()
        updatePhotosCountUI()
    }

    private fun initViews() {
        tvIntervalStatus = findViewById(R.id.tv_dialog_interval_status)
        tvPhotosCount = findViewById(R.id.tv_dialog_photos_count)

        chip15m = findViewById(R.id.dialog_chip_15m)
        chip20m = findViewById(R.id.dialog_chip_20m)
        chip30m = findViewById(R.id.dialog_chip_30m)
        chip60m = findViewById(R.id.dialog_chip_60m)
        chip3h = findViewById(R.id.dialog_chip_3h)
        chip6h = findViewById(R.id.dialog_chip_6h)
        chip24h = findViewById(R.id.dialog_chip_24h)
    }

    private fun setupListeners() {
        findViewById<TextView>(R.id.btn_dialog_close)?.setOnClickListener {
            finishWithResult()
        }

        findViewById<Button>(R.id.btn_dialog_done)?.setOnClickListener {
            finishWithResult()
        }

        findViewById<Button>(R.id.btn_dialog_add_photos)?.setOnClickListener {
            launchGalleryPicker()
        }

        findViewById<Button>(R.id.btn_dialog_open_vault)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btn_dialog_cycle_now)?.setOnClickListener {
            val photos = PhotoManager.getPhotoFileNames(this)
            if (photos.size > 1) {
                PhotoCycleScheduler.triggerCycleImmediately(this)
                Toast.makeText(this, "Switched to next photo!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Add more photos first!", Toast.LENGTH_SHORT).show()
            }
        }

        val intervalMap = listOf(
            chip15m to 15,
            chip20m to 20,
            chip30m to 30,
            chip60m to 60,
            chip3h to 180,
            chip6h to 360,
            chip24h to 1440
        )

        for ((chip, minutes) in intervalMap) {
            chip.setOnClickListener {
                PhotoManager.setIntervalMinutes(this, minutes)
                PhotoCycleScheduler.scheduleNextCycle(this)
                updateIntervalChipsUI()
                Toast.makeText(this, "Interval set to $minutes minutes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchGalleryPicker() {
        try {
            pickMultipleLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            fallbackPickerLauncher.launch(Intent.createChooser(intent, "Select Photos"))
        }
    }

    private fun updateIntervalChipsUI() {
        val current = PhotoManager.getIntervalMinutes(this)
        val chips = mapOf(
            15 to chip15m,
            20 to chip20m,
            30 to chip30m,
            60 to chip60m,
            180 to chip3h,
            360 to chip6h,
            1440 to chip24h
        )

        for ((minutes, chip) in chips) {
            val selected = (minutes == current)
            chip.setBackgroundResource(
                if (selected) R.drawable.pill_interval_active_bg else R.drawable.pill_interval_bg
            )
            chip.setTextColor(
                if (selected) Color.parseColor("#FFB703") else Color.parseColor("#9DA4B4")
            )
        }

        tvIntervalStatus.text = "Photos rotate automatically every $current minutes"
    }

    private fun updatePhotosCountUI() {
        val count = PhotoManager.getPhotoFileNames(this).size
        tvPhotosCount.text = "$count photo(s) in rotation"
    }

    private fun finishWithResult() {
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
        } else {
            setResult(RESULT_OK)
        }
        finish()
    }
}
