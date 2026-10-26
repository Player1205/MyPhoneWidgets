package com.antiprocrastination.daysleft

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
     * Renders the complete, pixel-perfect widget graphic canvas into an ARGB_8888 Bitmap:
     * - Fixes OEM launcher limitation (Samsung One UI ignores custom font & text shadow in RemoteViews).
     * - Renders authentic Playfair Display ExtraBold Italic directly on the canvas with hardware anti-aliasing.
     * - Multi-pass luminous Cyber Amber glow (inner tight halo + outer radiant aura).
     * - Complete 365 uniform starry grid (28x14).
     * - Dots directly under letter glyphs are softly dimmed (ZERO box overhang, zero borderlines).
     * - TODAY'S DOT is rendered on the top layer at 100% full intensity (Cyber Amber rim & dynamic white sweep fill).
     * - Quote rendered in Playfair Display Bold Italic with subtle drop shadow.
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

        // Text setup & exact measurements (Zero Box Overhang!)
        val heroText = "$daysLeft days left"
        val heroTextSize = 88f
        val heroX = width / 2f
        val heroY = 186f

        val heroMeasurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            heroTypeface?.let { typeface = it }
            textSize = heroTextSize
        }
        val heroWidth = heroMeasurePaint.measureText(heroText)
        val heroLeft = (width - heroWidth) / 2f
        val heroRight = heroLeft + heroWidth
        val heroTop = heroY - 68f
        val heroBottom = heroY + 20f

        val quoteText = "“$quote”"
        val quoteMeasurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            quoteTypeface?.let { typeface = it }
            textSize = 31f
        }
        val quoteWidth = quoteMeasurePaint.measureText(quoteText).coerceAtMost(600f)
        val quoteLeft = (width - quoteWidth) / 2f
        val quoteRight = quoteLeft + quoteWidth
        val quoteTop = 246f
        val quoteBottom = 320f

        val yearText = "in $year"
        val yearMeasurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 24f
        }
        val yearWidth = yearMeasurePaint.measureText(yearText)
        val yearLeft = 44f
        val yearRight = 44f + yearWidth
        val yearTop = 22f
        val yearBottom = 48f

        // Exact glyph-based dimming check (ZERO overhang on left, right, top, bottom)
        fun isUnderText(x: Float, y: Float): Boolean {
            if (x in (heroLeft - 4f)..(heroRight + 4f) && y in heroTop..heroBottom) return true
            if (x in (quoteLeft - 6f)..(quoteRight + 6f) && y in quoteTop..quoteBottom) return true
            if (x in (yearLeft - 4f)..(yearRight + 4f) && y in yearTop..yearBottom) return true
            return false
        }

        // Grid parameters: uniform 28 columns x 14 rows across entire widget
        val cols = 28
        val rows = 14
        val paddingX = 36f
        val paddingY = 26f
        val stepX = (width - paddingX * 2) / (cols - 1)
        val stepY = (height - paddingY * 2) / (rows - 1)

        // Radiant starlight dots outside text
        val pastPaintBright = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(85, 255, 255, 255) // Bright starlight (~33% opacity)
            style = Paint.Style.FILL
        }
        val futurePaintBright = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(50, 255, 255, 255) // Crisp hollow ring (~20% opacity)
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
        }

        // Softly dimmed dots directly under text glyphs (visible, but less bright)
        val pastPaintDimmed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(32, 255, 255, 255) // Softened so letters pop cleanly
            style = Paint.Style.FILL
        }
        val futurePaintDimmed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(20, 255, 255, 255) // Subtle ring
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }

        // Today's dot paints (100% full intensity Cyber Amber)
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

        // Step 1: Draw ambient 365 dots across the complete grid
        var todayX = -1f
        var todayY = -1f

        var day = 1
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (day > totalDays) break
                val cx = paddingX + c * stepX
                val cy = paddingY + r * stepY

                val under = isUnderText(cx, cy)

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
            textSize = 24f
            letterSpacing = 0.04f
        }
        canvas.drawText("in $year", 44f, 44f, yearPaint)

        val penPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 24f
        }
        canvas.drawText("✍️", width - 74f, 44f, penPaint)

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

        // Step 5: Draw Quote Section in Playfair Display Bold Italic
        val qLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9DA4B3")
            textSize = 19f
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText("quote by me:", width / 2f, 268f, qLabelPaint)

        val quoteTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            quoteTypeface?.let { typeface = it }
            textSize = 31f
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
            setShadowLayer(6f, 0f, 2f, Color.argb(150, 0, 0, 0))
        }

        if (quoteMeasurePaint.measureText(quoteText) > 600f) {
            val textPaint = TextPaint(quoteTextPaint).apply {
                textAlign = Paint.Align.LEFT
            }
            val sl = StaticLayout.Builder.obtain(quoteText, 0, quoteText.length, textPaint, 600)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setMaxLines(2)
                .setEllipsize(android.text.TextUtils.TruncateAt.END)
                .build()
            canvas.save()
            canvas.translate((width - 600f) / 2f, 282f)
            sl.draw(canvas)
            canvas.restore()
        } else {
            canvas.drawText(quoteText, width / 2f, 308f, quoteTextPaint)
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
        val paddingY = 26f
        val stepX = (width - paddingX * 2) / (cols - 1)
        val stepY = (height - paddingY * 2) / (rows - 1)

        val pastPaintBright = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(85, 255, 255, 255)
            style = Paint.Style.FILL
        }
        val futurePaintBright = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(50, 255, 255, 255)
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
