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
     * Generates a starry 365-day ambient dot matrix background bitmap:
     * - ZERO OVERLAP with text: dots wrap around "in 2026", "66 days left", and quote sections.
     * - Uniform distribution across top, sides, divider, and bottom margins (no Swiss-cheese holes).
     * - Past days: Translucent filled soft-white starlight dots
     * - Future days: Subtle hollow empty rings
     * - Today's dot: Distinctly larger with Cyber Amber ring & dynamic sweep fill representing elapsed day progress
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

        val cols = 42
        val rows = 18

        val paddingX = 28f
        val paddingY = 22f
        val stepX = (width - paddingX * 2) / (cols - 1)
        val stepY = (height - paddingY * 2) / (rows - 1)

        val pastPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(44, 255, 255, 255) // Soft translucent white (~17% opacity)
            style = Paint.Style.FILL
        }

        val futurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(22, 255, 255, 255) // Subtle hollow ring (~8% opacity)
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
        }

        val todayRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 183, 3) // Cyber Amber ring (#FFB703)
            style = Paint.Style.STROKE
            strokeWidth = 2.2f
        }

        val todayFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 255, 255) // Crisp white filling
            style = Paint.Style.FILL
        }

        val todayGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 255, 183, 3)
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
        }

        // Collect all non-overlapping wrap-around dot positions
        val validPositions = mutableListOf<Pair<Float, Float>>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cx = paddingX + c * stepX
                val cy = paddingY + r * stepY

                // Clean organic exclusion zones to prevent ANY collision with text:
                // 1. Top-left "in 2026"
                val inTopLeft = cx < 165f && cy < 65f
                // 2. Top-right "✍️" edit shortcut
                val inTopRight = cx > (width - 75f) && cy < 65f
                // 3. Center "66 days left" hero text
                val inCenterHero = cx > 80f && cx < 640f && cy > 92f && cy < 205f
                // 4. Bottom quote section ("quote by me:" + quote text)
                val inBottomQuote = cx > 140f && cx < 580f && cy > 240f && cy < 330f

                if (!inTopLeft && !inTopRight && !inCenterHero && !inBottomQuote) {
                    validPositions.add(Pair(cx, cy))
                }
            }
        }

        // Uniformly sample across ALL valid positions so dots cover the entire height
        // (top rows, side flanks, middle divider, and bottom margins) without bottom truncation
        val selectedPositions = if (validPositions.size >= totalDays) {
            val step = validPositions.size.toDouble() / totalDays
            (0 until totalDays).map { validPositions[(it * step).toInt()] }
        } else {
            validPositions
        }

        val renderLimit = totalDays.coerceAtMost(selectedPositions.size)
        for (i in 0 until renderLimit) {
            val (cx, cy) = selectedPositions[i]
            val dayNumber = i + 1

            if (dayNumber < dayOfYear) {
                // Past days: Soft translucent filled white dot
                canvas.drawCircle(cx, cy, 2.2f, pastPaint)
            } else if (dayNumber == dayOfYear) {
                // Today: Distinctly larger dot with Cyber Amber ring & dynamic white sweep fill
                val radius = 5.8f
                canvas.drawCircle(cx, cy, radius + 2.5f, todayGlowPaint)
                canvas.drawCircle(cx, cy, radius, todayRingPaint)

                if (dayProgress > 0.02f) {
                    val oval = RectF(
                        cx - radius + 0.6f,
                        cy - radius + 0.6f,
                        cx + radius - 0.6f,
                        cy + radius - 0.6f
                    )
                    canvas.drawArc(oval, -90f, dayProgress * 360f, true, todayFillPaint)
                }
            } else {
                // Future days: Subtle hollow empty ring
                canvas.drawCircle(cx, cy, 1.8f, futurePaint)
            }
        }

        return bitmap
    }
}
