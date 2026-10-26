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
     * - Past days: Translucent filled soft-white dots
     * - Future days: Subtle hollow empty rings
     * - Today's dot: Larger with dynamic sweep fill representing elapsed day progress
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
        val rows = 13

        val paddingX = 42f
        val paddingY = 36f
        val stepX = (width - paddingX * 2) / (cols - 1)
        val stepY = (height - paddingY * 2) / (rows - 1)

        val pastPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(48, 255, 255, 255) // Soft translucent white (~19% opacity)
            style = Paint.Style.FILL
        }

        val futurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(22, 255, 255, 255) // Subtle hollow ring (~9% opacity)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val todayRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(230, 255, 183, 3) // Cyber Amber ring
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }

        val todayFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 255, 255) // Crisp white filling
            style = Paint.Style.FILL
        }

        val todayGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(70, 255, 183, 3)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        var day = 1
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (day > totalDays) break
                val cx = paddingX + c * stepX
                val cy = paddingY + r * stepY

                if (day < dayOfYear) {
                    // Past days: Translucent filled white dot
                    canvas.drawCircle(cx, cy, 3.5f, pastPaint)
                } else if (day == dayOfYear) {
                    // Current day: Larger dot (radius 7.5f) with dynamic white filling
                    val radius = 7.5f
                    canvas.drawCircle(cx, cy, radius + 3.5f, todayGlowPaint)
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
                    // Future days: Subtle hollow ring
                    canvas.drawCircle(cx, cy, 3.2f, futurePaint)
                }
                day++
            }
        }

        return bitmap
    }
}