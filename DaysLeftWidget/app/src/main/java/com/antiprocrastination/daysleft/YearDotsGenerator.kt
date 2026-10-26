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
     * Generates an ambient 365-day dot matrix background bitmap:
     * - ZERO OVERLAP with text: dots wrap around "in 2026", "66 days left", and quote sections.
     * - Past days: Translucent filled soft-white dots
     * - Future days: Subtle hollow empty rings
     * - Today's dot: Distinctly larger with dynamic sweep fill representing elapsed day progress
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

        val cols = 44
        val rows = 20

        val paddingX = 25f
        val paddingY = 20f
        val stepX = (width - paddingX * 2) / (cols - 1)
        val stepY = (height - paddingY * 2) / (rows - 1)

        val pastPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(45, 255, 255, 255) // Soft translucent white (~18% opacity)
            style = Paint.Style.FILL
        }

        val futurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(20, 255, 255, 255) // Subtle hollow ring (~8% opacity)
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
        }

        val todayRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(240, 255, 183, 3) // Cyber Amber ring
            style = Paint.Style.STROKE
            strokeWidth = 2.4f
        }

        val todayFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 255, 255) // Crisp white filling
            style = Paint.Style.FILL
        }

        val todayGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 255, 183, 3)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }

        // Collect all non-overlapping wrap-around dot positions
        val validPositions = mutableListOf<Pair<Float, Float>>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cx = paddingX + c * stepX
                val cy = paddingY + r * stepY

                // Exclusion zones to prevent ANY overlap with text:
                // 1. Top-left "in 2026"
                val inTopLeft = cx < 210f && cy < 65f
                // 2. Top-right "✍️"
                val inTopRight = cx > (width - 90f) && cy < 65f
                // 3. Center "66 days left" (including wide glow aura)
                val inCenterHero = cx > 85f && cx < 635f && cy > 95f && cy < 240f
                // 4. Bottom quote section ("QUOTE BY ME:" + quote text)
                val inBottomQuote = cx > 115f && cx < 605f && cy > 250f

                if (!inTopLeft && !inTopRight && !inCenterHero && !inBottomQuote) {
                    validPositions.add(Pair(cx, cy))
                }
            }
        }

        // Render exactly totalDays dots wrapping around the text
        val renderLimit = totalDays.coerceAtMost(validPositions.size)
        for (i in 0 until renderLimit) {
            val (cx, cy) = validPositions[i]
            val dayNumber = i + 1

            if (dayNumber < dayOfYear) {
                // Past days: Soft translucent filled white dot
                canvas.drawCircle(cx, cy, 3.2f, pastPaint)
            } else if (dayNumber == dayOfYear) {
                // Today: Distinctly larger dot with Cyber Amber ring & dynamic white sweep fill
                val radius = 7.0f
                canvas.drawCircle(cx, cy, radius + 3.5f, todayGlowPaint)
                canvas.drawCircle(cx, cy, radius, todayRingPaint)

                if (dayProgress > 0.02f) {
                    val oval = RectF(
                        cx - radius + 0.5f,
                        cy - radius + 0.5f,
                        cx + radius - 0.5f,
                        cy + radius - 0.5f
                    )
                    canvas.drawArc(oval, -90f, dayProgress * 360f, true, todayFillPaint)
                }
            } else {
                // Future days: Subtle hollow empty ring
                canvas.drawCircle(cx, cy, 3.0f, futurePaint)
            }
        }

        return bitmap
    }
}