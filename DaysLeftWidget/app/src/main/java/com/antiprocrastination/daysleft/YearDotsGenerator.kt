package com.antiprocrastination.daysleft

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.time.LocalDate
import java.time.LocalTime

object YearDotsGenerator {

    /**
     * Generates a pure 365-day ambient starry grid:
     * - ZERO rectangular background boxes or borderlines (no excess background above, left, or right).
     * - Dots directly under text letters are softly dimmed for perfect legibility.
     * - Dots around text are bright, radiant starlight dots.
     * - TODAY'S DOT is rendered on the top layer at 100% full brightness (Cyber Amber rim & dynamic white sweep fill).
     */
    fun generateYearDotsBitmap(
        width: Int = 720,
        height: Int = 360,
        today: LocalDate = LocalDate.now(),
        now: LocalTime = LocalTime.now()
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

        // Exact letter bounding areas (strictly under the text glyphs, 0px box overhang)
        fun isUnderText(x: Float, y: Float): Boolean {
            // Under hero text letters "66 days left"
            if (x in 100f..620f && y in 126f..215f) return true
            // Under quote text letters
            if (x in 155f..565f && y in 244f..324f) return true
            // Under "in 2026"
            if (x in 36f..130f && y in 24f..56f) return true
            return false
        }

        // Radiant starlight dots outside text
        val pastPaintBright = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(76, 255, 255, 255) // Bright starlight (~30% opacity)
            style = Paint.Style.FILL
        }
        val futurePaintBright = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(42, 255, 255, 255) // Crisp hollow ring (~16% opacity)
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
        }

        // Softly dimmed dots directly under text glyphs
        val pastPaintDimmed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(22, 255, 255, 255) // Softened so letters pop cleanly
            style = Paint.Style.FILL
        }
        val futurePaintDimmed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(14, 255, 255, 255) // Subtle
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }

        // Today's dot paints (100% full intensity)
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

        // Step 1: Draw ambient dots across the complete grid
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

        // Step 2: Draw Today's Dot ON TOP (100% visible, bright Cyber Amber)
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

        return bitmap
    }
}
