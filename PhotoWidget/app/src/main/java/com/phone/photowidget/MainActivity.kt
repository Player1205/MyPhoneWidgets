package com.phone.photowidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var tvPreviewStatus: TextView
    private lateinit var tvVaultCount: TextView
    private lateinit var tvEmptyVault: TextView
    private lateinit var scrollPhotos: HorizontalScrollView
    private lateinit var containerPhotos: LinearLayout
    private lateinit var tvIntervalDescription: TextView

    // Interval Chips
    private lateinit var chip15m: TextView
    private lateinit var chip20m: TextView
    private lateinit var chip30m: TextView
    private lateinit var chip60m: TextView
    private lateinit var chip3h: TextView
    private lateinit var chip6h: TextView
    private lateinit var chip24h: TextView

    // Shape Chips
    private lateinit var chipShapeOval: TextView
    private lateinit var chipShapeCircle: TextView
    private lateinit var chipShapeRounded: TextView

    // Border Chips
    private lateinit var chipBorderGoldGlow: TextView
    private lateinit var chipBorderGold: TextView
    private lateinit var chipBorderDark: TextView
    private lateinit var chipBorderNone: TextView

    // Multi-Image Picker Launcher (Android 13+ Photo Picker with backwards-compatible Google Play Services)
    private val pickMultipleMediaLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val count = PhotoManager.addPhotosFromUris(this, uris)
            refreshAllUI()
            PhotoWidgetProvider.updateAllWidgets(this)
            PhotoCycleScheduler.scheduleNextCycle(this)
            Toast.makeText(this, "Added $count photo(s) to widget vault!", Toast.LENGTH_SHORT).show()
        }
    }

    // Fallback Intent Launcher for devices without Photo Picker
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
                refreshAllUI()
                PhotoWidgetProvider.updateAllWidgets(this)
                PhotoCycleScheduler.scheduleNextCycle(this)
                Toast.makeText(this, "Added $count photo(s) to widget vault!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        refreshAllUI()

        // Ensure rotation scheduler is running
        PhotoCycleScheduler.scheduleNextCycle(this)
    }

    override fun onResume() {
        super.onResume()
        refreshAllUI()
    }

    private fun initViews() {
        ivPreview = findViewById(R.id.iv_preview)
        tvPreviewStatus = findViewById(R.id.tv_preview_status)
        tvVaultCount = findViewById(R.id.tv_vault_count)
        tvEmptyVault = findViewById(R.id.tv_empty_vault)
        scrollPhotos = findViewById(R.id.scroll_photos)
        containerPhotos = findViewById(R.id.container_photos)
        tvIntervalDescription = findViewById(R.id.tv_interval_description)

        chip15m = findViewById(R.id.chip_15m)
        chip20m = findViewById(R.id.chip_20m)
        chip30m = findViewById(R.id.chip_30m)
        chip60m = findViewById(R.id.chip_60m)
        chip3h = findViewById(R.id.chip_3h)
        chip6h = findViewById(R.id.chip_6h)
        chip24h = findViewById(R.id.chip_24h)

        chipShapeOval = findViewById(R.id.chip_shape_oval)
        chipShapeCircle = findViewById(R.id.chip_shape_circle)
        chipShapeRounded = findViewById(R.id.chip_shape_rounded)

        chipBorderGoldGlow = findViewById(R.id.chip_border_gold_glow)
        chipBorderGold = findViewById(R.id.chip_border_gold)
        chipBorderDark = findViewById(R.id.chip_border_dark)
        chipBorderNone = findViewById(R.id.chip_border_none)
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btn_pick_photos)?.setOnClickListener {
            launchPhotoPicker()
        }

        findViewById<Button>(R.id.btn_prev_preview)?.setOnClickListener {
            val photos = PhotoManager.getPhotoFileNames(this)
            if (photos.size > 1) {
                PhotoManager.cycleToPrev(this)
                refreshAllUI()
                PhotoWidgetProvider.updateAllWidgets(this)
            }
        }

        findViewById<Button>(R.id.btn_next_preview)?.setOnClickListener {
            val photos = PhotoManager.getPhotoFileNames(this)
            if (photos.size > 1) {
                PhotoManager.cycleToNext(this)
                refreshAllUI()
                PhotoWidgetProvider.updateAllWidgets(this)
            }
        }

        findViewById<Button>(R.id.btn_cycle_now)?.setOnClickListener {
            val photos = PhotoManager.getPhotoFileNames(this)
            if (photos.size > 1) {
                PhotoCycleScheduler.triggerCycleImmediately(this)
                refreshAllUI()
                Toast.makeText(this, "Cycled to next photo!", Toast.LENGTH_SHORT).show()
            } else if (photos.isEmpty()) {
                launchPhotoPicker()
            } else {
                Toast.makeText(this, "Add more photos to cycle through them!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_add_to_home)?.setOnClickListener {
            requestPinWidget()
        }

        // Interval chips click handlers
        val intervalChips = listOf(
            chip15m to 15,
            chip20m to 20,
            chip30m to 30,
            chip60m to 60,
            chip3h to 180,
            chip6h to 360,
            chip24h to 1440
        )

        for ((chip, minutes) in intervalChips) {
            chip.setOnClickListener {
                PhotoManager.setIntervalMinutes(this, minutes)
                PhotoCycleScheduler.scheduleNextCycle(this)
                updateIntervalChipsUI()
                updatePreviewStatus()
                Toast.makeText(this, "Interval set to $minutes minutes", Toast.LENGTH_SHORT).show()
            }
        }

        // Shape chips click handlers
        chipShapeOval.setOnClickListener { selectShape(PhotoManager.SHAPE_OVAL) }
        chipShapeCircle.setOnClickListener { selectShape(PhotoManager.SHAPE_CIRCLE) }
        chipShapeRounded.setOnClickListener { selectShape(PhotoManager.SHAPE_ROUNDED) }

        // Border chips click handlers
        chipBorderGoldGlow.setOnClickListener { selectBorder(PhotoManager.BORDER_GOLD_GLOW) }
        chipBorderGold.setOnClickListener { selectBorder(PhotoManager.BORDER_GOLD) }
        chipBorderDark.setOnClickListener { selectBorder(PhotoManager.BORDER_DARK) }
        chipBorderNone.setOnClickListener { selectBorder(PhotoManager.BORDER_NONE) }
    }

    private fun selectShape(shape: String) {
        PhotoManager.setShape(this, shape)
        updateShapeChipsUI()
        updatePreview()
        PhotoWidgetProvider.updateAllWidgets(this)
    }

    private fun selectBorder(borderStyle: String) {
        PhotoManager.setBorderStyle(this, borderStyle)
        updateBorderChipsUI()
        updatePreview()
        PhotoWidgetProvider.updateAllWidgets(this)
    }

    private fun launchPhotoPicker() {
        try {
            pickMultipleMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (e: Exception) {
            // Fallback to generic image intent
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            fallbackPickerLauncher.launch(Intent.createChooser(intent, "Select Photos"))
        }
    }

    private fun refreshAllUI() {
        updatePreview()
        updatePreviewStatus()
        updateIntervalChipsUI()
        updateShapeChipsUI()
        updateBorderChipsUI()
        refreshVaultPhotosUI()
    }

    private fun updatePreview() {
        val activePhoto = PhotoManager.getActivePhotoFile(this)
        val shape = PhotoManager.getShape(this)
        val borderStyle = PhotoManager.getBorderStyle(this)

        val bitmap = PhotoBitmapRenderer.renderWidgetBitmap(
            context = this,
            photoFile = activePhoto,
            width = 720,
            height = 720,
            shape = shape,
            borderStyle = borderStyle
        )
        ivPreview.setImageBitmap(bitmap)
    }

    private fun updatePreviewStatus() {
        val photos = PhotoManager.getPhotoFileNames(this)
        if (photos.isEmpty()) {
            tvPreviewStatus.text = "No photos added • Tap below to add"
        } else {
            val activeIdx = PhotoManager.getActiveIndex(this)
            val remainingMin = PhotoManager.getRemainingMinutes(this)
            tvPreviewStatus.text = "Photo ${activeIdx + 1} of ${photos.size} • Next switch in ~${remainingMin}m"
        }
    }

    private fun updateIntervalChipsUI() {
        val currentInterval = PhotoManager.getIntervalMinutes(this)
        val map = mapOf(
            15 to chip15m,
            20 to chip20m,
            30 to chip30m,
            60 to chip60m,
            180 to chip3h,
            360 to chip6h,
            1440 to chip24h
        )

        for ((minutes, chip) in map) {
            val isSelected = (minutes == currentInterval)
            chip.setBackgroundResource(
                if (isSelected) R.drawable.pill_interval_active_bg else R.drawable.pill_interval_bg
            )
            chip.setTextColor(
                if (isSelected) Color.parseColor("#FFB703") else Color.parseColor("#9DA4B4")
            )
        }

        val text = when (currentInterval) {
            15 -> "Photos switch automatically every 15 minutes."
            20 -> "Photos switch automatically every 20 minutes."
            30 -> "Photos switch automatically every 30 minutes."
            60 -> "Photos switch automatically every 1 hour (60 minutes)."
            180 -> "Photos switch automatically every 3 hours."
            360 -> "Photos switch automatically every 6 hours."
            1440 -> "Photos switch automatically once every 24 hours (Daily)."
            else -> "Photos switch automatically every $currentInterval minutes."
        }
        tvIntervalDescription.text = text
    }

    private fun updateShapeChipsUI() {
        val currentShape = PhotoManager.getShape(this)
        val shapeMap = mapOf(
            PhotoManager.SHAPE_OVAL to chipShapeOval,
            PhotoManager.SHAPE_CIRCLE to chipShapeCircle,
            PhotoManager.SHAPE_ROUNDED to chipShapeRounded
        )

        for ((shape, chip) in shapeMap) {
            val isSelected = (shape == currentShape)
            chip.setBackgroundResource(
                if (isSelected) R.drawable.pill_interval_active_bg else R.drawable.pill_interval_bg
            )
            chip.setTextColor(
                if (isSelected) Color.parseColor("#FFB703") else Color.parseColor("#9DA4B4")
            )
        }
    }

    private fun updateBorderChipsUI() {
        val currentBorder = PhotoManager.getBorderStyle(this)
        val borderMap = mapOf(
            PhotoManager.BORDER_GOLD_GLOW to chipBorderGoldGlow,
            PhotoManager.BORDER_GOLD to chipBorderGold,
            PhotoManager.BORDER_DARK to chipBorderDark,
            PhotoManager.BORDER_NONE to chipBorderNone
        )

        for ((border, chip) in borderMap) {
            val isSelected = (border == currentBorder)
            chip.setBackgroundResource(
                if (isSelected) R.drawable.pill_interval_active_bg else R.drawable.pill_interval_bg
            )
            chip.setTextColor(
                if (isSelected) Color.parseColor("#FFB703") else Color.parseColor("#9DA4B4")
            )
        }
    }

    private fun refreshVaultPhotosUI() {
        val photos = PhotoManager.getAllPhotoFiles(this)
        val activeIndex = PhotoManager.getActiveIndex(this)

        tvVaultCount.text = "${photos.size} photo(s) in rotation"
        containerPhotos.removeAllViews()

        if (photos.isEmpty()) {
            tvEmptyVault.visibility = View.VISIBLE
            scrollPhotos.visibility = View.GONE
        } else {
            tvEmptyVault.visibility = View.GONE
            scrollPhotos.visibility = View.VISIBLE

            val inflater = LayoutInflater.from(this)
            for ((index, file) in photos.withIndex()) {
                val itemView = inflater.inflate(R.layout.item_photo_card, containerPhotos, false)
                val ivThumb = itemView.findViewById<ImageView>(R.id.iv_thumb)
                val tvActiveBadge = itemView.findViewById<TextView>(R.id.tv_active_badge)
                val btnSetActive = itemView.findViewById<Button>(R.id.btn_set_active)
                val btnDelete = itemView.findViewById<TextView>(R.id.btn_delete)

                // Load thumbnail safely
                try {
                    val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                    val thumbBitmap = BitmapFactory.decodeFile(file.absolutePath, opts)
                    ivThumb.setImageBitmap(thumbBitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val isActive = (index == activeIndex)
                if (isActive) {
                    tvActiveBadge.visibility = View.VISIBLE
                    btnSetActive.text = "Showing Now"
                    btnSetActive.setTextColor(Color.parseColor("#FFB703"))
                    btnSetActive.isEnabled = false
                } else {
                    tvActiveBadge.visibility = View.GONE
                    btnSetActive.text = "Set Active"
                    btnSetActive.setTextColor(Color.WHITE)
                    btnSetActive.isEnabled = true
                    btnSetActive.setOnClickListener {
                        PhotoManager.setActiveIndex(this, index)
                        refreshAllUI()
                        PhotoWidgetProvider.updateAllWidgets(this)
                        Toast.makeText(this, "Set as active photo!", Toast.LENGTH_SHORT).show()
                    }
                }

                btnDelete.setOnClickListener {
                    PhotoManager.deletePhoto(this, index)
                    refreshAllUI()
                    PhotoWidgetProvider.updateAllWidgets(this)
                    Toast.makeText(this, "Photo removed from rotation", Toast.LENGTH_SHORT).show()
                }

                containerPhotos.addView(itemView)
            }
        }
    }

    private fun requestPinWidget() {
        val appWidgetManager = getSystemService(AppWidgetManager::class.java)
        val myProvider = ComponentName(this, PhotoWidgetProvider::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            appWidgetManager.isRequestPinAppWidgetSupported
        ) {
            appWidgetManager.requestPinAppWidget(myProvider, null, null)
            Toast.makeText(
                this,
                "Follow Samsung One UI prompt to place widget on Home Screen!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this,
                "Long-press your Home Screen, choose 'Widgets' > 'Photo Cycle Widget'",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
