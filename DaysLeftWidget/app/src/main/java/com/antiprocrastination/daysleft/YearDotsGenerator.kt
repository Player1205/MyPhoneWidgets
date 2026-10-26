package com.antiprocrastination.daysleft

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import java.time.LocalDate
import java.time.LocalTime

object YearDotsGenerator {

    private var cachedHeroTypeface: Typeface? = null
    private var cachedQuoteTypeface: Typeface? = null

    private fun getHeroTypeface(context: Context): Typeface? {
        if (cachedHeroTypeface != null) return cachedHeroTypeface
        cachedHeroTypeface = try {
            ResourcesCompat.getFont(context, R.font.playfair_display_italic)
        } catch (e: Exception) {
            null
        }
        return cachedHeroTypeface
    }

    private fun getQuoteTypeface(context: Context): Typeface? {
        if (cachedQuoteTypeface != null) return cachedQuoteTypeface
        cachedQuoteTypeface = try {
            ResourcesCompat.getFont(context, R.font.playfair_display_quote)
        } catch (e: Exception) {
            getHeroTypeface(context)
        }
        return cachedQuoteTypeface
    }

    /**
     * Renders the complete, pixel-perfect designer widget graphic canvas into an ARGB_8888 Bitmap:
     * - Custom stepped silhouette: Top wide rounded card + bottom dynamic quote tab protruding downwards.
     * - Bottom tab width dynamically adapts to the exact size of the user's quote (Zero Extra Background!).
     * - Fixes OEM launcher limitation (Samsung One UI ignores custom font & text shadow in RemoteViews).
     * - Renders authentic Playfair Display ExtraBold Italic directly on the canvas with hardware anti-aliasing.
     * - Multi-pass luminous Cyber Amber glow (inner tight halo + outer radiant aura).
     * - 365 uniform starry grid (28x14) across the main upper card.
     * - TODAY'S DOT rendered on top layer at 100% full intensity (Cyber Amber rim & dynamic white sweep fill).
     * - Quote rendered in Playfair Display Bold Italic inside the dedicated bottom tab.
     */
    fun generateWidgetBitmap(
        context: Context,
        quote: String,
        width: Int = 720,
        height: Int = 360,
        today: LocalDate = LocalDate.now(),
        now: LocalTime = LocalTime.now()
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val totalDays = if (today.isLeapYear) 366 else 365
        val dayOfYear = today.dayOfYear
        val daysLeft = totalDays - dayOfYear
        val year = today.year
        val dayProgress = now.toSecondOfDay().toFloat() / 86400f

        val heroTypeface = getHeroTypeface(context)
        val quoteTypeface = getQuoteTypeface(context)

        // 1. Measure Quote to dynamically size the bottom tab
        val quoteText = "“$quote”"
        val quoteMeasurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            quoteTypeface?.let { typeface = it }
            textSize = 28f
        }
        val measuredQuoteWidth = quoteMeasurePaint.measureText(quoteText)

        val qLabelMeasurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 16f
            letterSpacing = 0.08f
        }
        val labelWidth = qLabelMeasurePaint.measureText("quote by me:")

        // Calculate dynamic tab width: wraps quote text with 40px padding on each side
        val minTabWidth = 280f
        val maxTabWidth = 630f
        val naturalWidth = Math.max(measuredQuoteWidth + 80f, labelWidth + 80f)
        val tabWidth = naturalWidth.coerceIn(minTabWidth, maxTabWidth)

        val tabLeft = (width - tabWidth) / 2f
        val tabRight = tabLeft + tabWidth

        // 2. Geometry boundaries of the stepped silhouette
        val mainTop = 10f
        val mainBottom = 248f
        val mainLeft = 14f
        val mainRight = width - 14f
        val mainRadius = 28f

        val tabTop = mainBottom
        val tabBottom = 348f
        val tabRadius = 22f

        // 3. Construct unified stepped silhouette Path
        val backgroundPath = Path().apply {
            // Main upper rounded rectangle
            addRoundRect(
                RectF(mainLeft, mainTop, mainRight, mainBottom),
                mainRadius,
                mainRadius,
                Path.Direction.CW
            )
        }

        val tabRadii = floatArrayOf(
            0f, 0f, // Top-left (merges with main card)
            0f, 0f, // Top-right (merges with main card)
            tabRadius, tabRadius, // Bottom-right
            tabRadius, tabRadius  // Bottom-left
        )
        val quoteTabPath = Path().apply {
            addRoundRect(
                RectF(tabLeft, mainBottom - 4f, tabRight, tabBottom),
                tabRadii,
                Path.Direction.CW
            )
        }

        // Union main card and quote tab into one seamless continuous shape
        backgroundPath.op(quoteTabPath, Path.Op.UNION)

        // Fill with frosted dark obsidian glass (translucent, shows wallpaper through)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(225, 9, 12, 19)
            style = Paint.Style.FILL
        }
        canvas.drawPath(backgroundPath, bgPaint)

        // 4. Measure Hero Text & dimming bounds (Zero Box Overhang!)
        val heroText = "$daysLeft days left"
        val heroTextSize = 80f
        val heroX = width / 2f
        val heroY = 166f

        val heroMeasurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            heroTypeface?.let { typeface = it }
            textSize = heroTextSize
        }
        val heroWidth = heroMeasurePaint.measureText(heroText)
        val heroLeft = (width - heroWidth) / 2f
        val heroRight = heroLeft + heroWidth
        val heroTop = heroY - 62f
        val heroBottom = heroY + 18f

        fun isUnderHeroText(x: Float, y: Float): Boolean {
            return x in (heroLeft - 4f)..(heroRight + 4f) && y in heroTop..heroBottom
        }

        // 5. Grid parameters for 365 Year Dots (28 cols x 14 rows inside main upper card)
        val cols = 28
        val rows = 14
        val padX = 36f
        val padY = 22f
        val stepX = (width - padX * 2) / (cols - 1)
        val stepY = (mainBottom - padY - 18f) / (rows - 1)

        val pastPaintBright = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 255, 255, 255) // Bright starlight (~31% opacity)
            style = Paint.Style.FILL
        }
        val futurePaintBright = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(45, 255, 255, 255) // Crisp hollow ring (~18% opacity)
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
        }
        val pastPaintDimmed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(24, 255, 255, 255) // Softened under letters
            style = Paint.Style.FILL
        }
        val futurePaintDimmed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(14, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }

        val todayRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 183, 3) // Cyber Amber (#FFB703)
            style = Paint.Style.STROKE
            strokeWidth = 2.4f
        }
        val todayFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 255, 255) // Crisp white filling
            style = Paint.Style.FILL
        }
        val todayGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 255, 183, 3)
            style = Paint.Style.STROKE
            strokeWidth = 2.0f
        }

        // Step 1: Draw ambient dots across the main upper card
        var todayX = -1f
        var todayY = -1f

        var day = 1
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (day > totalDays) break
                val cx = padX + c * stepX
                val cy = padY + r * stepY

                val under = isUnderHeroText(cx, cy)

                if (day == dayOfYear) {
                    todayX = cx
                    todayY = cy
                } else if (day < dayOfYear) {
                    val paint = if (under) pastPaintDimmed else pastPaintBright
                    canvas.drawCircle(cx, cy, 2.8f, paint)
                } else {
                    val paint = if (under) futurePaintDimmed else futurePaintBright
                    canvas.drawCircle(cx, cy, 2.4f, paint)
                }
                day++
            }
        }

        // Step 2: Draw Today's Dot ON TOP (100% visible, bright Cyber Amber with dynamic sweep)
        if (todayX >= 0f && todayY >= 0f) {
            val radius = 6.2f
            canvas.drawCircle(todayX, todayY, radius + 2.5f, todayGlowPaint)
            canvas.drawCircle(todayX, todayY, radius, todayRingPaint)

            if (dayProgress > 0.02f) {
                val oval = RectF(
                    todayX - radius + 0.6f,
                    todayY - radius + 0.6f,
                    todayX + radius - 0.6f,
                    todayY + radius - 0.6f
                )
                canvas.drawArc(oval, -90f, dayProgress * 360f, true, todayFillPaint)
            }
        }

        // Step 3: Draw Top Header
        val yearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A0A7B8")
            textSize = 22f
            letterSpacing = 0.04f
        }
        canvas.drawText("in $year", 44f, 42f, yearPaint)

        val penPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 22f
        }
        canvas.drawText("✍️", width - 72f, 42f, penPaint)

        // Step 4: Draw Hero "$daysLeft days left" in Playfair Display ExtraBold Italic with Cyber Amber Glow
        // Outer radiant aura
        val outerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            heroTypeface?.let { typeface = it }
            textSize = heroTextSize
            textAlign = Paint.Align.CENTER
            color = Color.argb(125, 255, 183, 3)
            maskFilter = BlurMaskFilter(22f, BlurMaskFilter.Blur.NORMAL)
            isDither = true
        }
        canvas.drawText(heroText, heroX, heroY, outerGlowPaint)

        // Inner tight glow halo
        val innerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            heroTypeface?.let { typeface = it }
            textSize = heroTextSize
            textAlign = Paint.Align.CENTER
            color = Color.argb(220, 255, 183, 3)
            maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
            isDither = true
        }
        canvas.drawText(heroText, heroX, heroY, innerGlowPaint)

        // Crisp foreground text in rich Cyber Amber (#FFBA08)
        val heroTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            heroTypeface?.let { typeface = it }
            textSize = heroTextSize
            textAlign = Paint.Align.CENTER
            color = Color.parseColor("#FFBA08")
            style = Paint.Style.FILL
            isDither = true
            setShadowLayer(4f, 0f, 1f, Color.argb(100, 0, 0, 0))
        }
        canvas.drawText(heroText, heroX, heroY, heroTextPaint)

        // Step 5: Draw Quote Section inside the Dynamic Bottom Tab
        val qLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9DA4B3")
            textSize = 16f
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText("quote by me:", width / 2f, tabTop + 24f, qLabelPaint)

        val quoteTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            quoteTypeface?.let { typeface = it }
            textSize = 28f
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
            setShadowLayer(4f, 0f, 1f, Color.argb(130, 0, 0, 0))
        }

        if (measuredQuoteWidth > (tabWidth - 40f)) {
            val textPaint = TextPaint(quoteTextPaint).apply {
                textAlign = Paint.Align.LEFT
            }
            val wrapWidth = (tabWidth - 40f).toInt()
            val sl = StaticLayout.Builder.obtain(quoteText, 0, quoteText.length, textPaint, wrapWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setMaxLines(2)
                .setEllipsize(android.text.TextUtils.TruncateAt.END)
                .build()
            canvas.save()
            canvas.translate((width - wrapWidth) / 2f, tabTop + 36f)
            sl.draw(canvas)
            canvas.restore()
        } else {
            canvas.drawText(quoteText, width / 2f, tabTop + 62f, quoteTextPaint)
        }

        return bitmap
    }

    // Backwards compatibility overload
    fun generateYearDotsBitmap(
        today: LocalDate = LocalDate.now(),
        now: LocalTime = LocalTime.now(),
        width: Int = 720,
        height: Int = 360
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val totalDays = if (today.isLeapYear) 366 else 365
        val dayOfYear = today.dayOfYear
        val dayProgress = now.toSecondOfDay().toFloat() / 86400f
        val cols = 28
        val rows = 14
        val paddingX = 36f
        val paddingY = 22f
        val stepX = (width - paddingX * 2) / (cols - 1)
        val stepY = (248f - paddingY - 18f) / (rows - 1)

        val pastPaintBright = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 255, 255, 255)
            style = Paint.Style.FILL
        }
        val futurePaintBright = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(45, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
        }
        val todayRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 183, 3)
            style = Paint.Style.STROKE
            strokeWidth = 2.4f
        }
        val todayFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 255, 255)
            style = Paint.Style.FILL
        }

        var day = 1
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (day > totalDays) break
                val cx = paddingX + c * stepX
                val cy = paddingY + r * stepY
                if (day < dayOfYear) {
                    canvas.drawCircle(cx, cy, 2.8f, pastPaintBright)
                } else if (day == dayOfYear) {
                    val radius = 6.2f
                    canvas.drawCircle(cx, cy, radius, todayRingPaint)
                    if (dayProgress > 0.02f) {
                        val oval = RectF(cx - radius + 0.6f, cy - radius + 0.6f, cx + radius - 0.6f, cy + radius - 0.6f)
                        canvas.drawArc(oval, -90f, dayProgress * 360f, true, todayFillPaint)
                    }
                } else {
                    canvas.drawCircle(cx, cy, 2.4f, futurePaintBright)
                }
                day++
            }
        }
        return bitmap
    }
}
