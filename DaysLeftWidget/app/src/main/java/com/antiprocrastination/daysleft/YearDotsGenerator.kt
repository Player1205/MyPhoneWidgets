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
     * Generates a complete, uniform 365-day ambient starry grid across the entire widget:
     * - Perfect 28x14 geometric matrix (no wrap-around holes or cutouts).
     * - Dots flow continuously across the entire card.
     * - Text elements sit on translucent frosted glass plinths that gracefully attenuate
     *   the dots underneath them for crisp readability.
     * - Past days: Soft translucent starlight dots
     * - Future days: Subtle hollow empty rings
     * - Today's dot: Distinctly larger with Cyber Amber ring & dynamic white sweep fill
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

        val pastPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(46, 255, 255, 255) // Soft translucent white (~18% opacity)
            style = Paint.Style.FILL
        }

        val futurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(22, 255, 255, 255) // Subtle hollow ring (~9% opacity)
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
        }

        val todayRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 183, 3) // Cyber Amber ring (#FFB703)
            style = Paint.Style.STROKE
            strokeWidth = 2.4f
        }

        val todayFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 255, 255) // Crisp white filling
            style = Paint.Style.FILL
        }

        val todayGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 255, 183, 3)
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
        }

        var day = 1
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (day > totalDays) break
                val cx = paddingX + c * stepX
                val cy = paddingY + r * stepY

                if (day < dayOfYear) {
                    // Past days: Translucent filled white dot
                    canvas.drawCircle(cx, cy, 2.8f, pastPaint)
                } else if (day == dayOfYear) {
                    // Today: Distinctly larger dot with Cyber Amber ring & dynamic white sweep fill
                    val radius = 6.0f
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
                    canvas.drawCircle(cx, cy, 2.4f, futurePaint)
                }
                day++
            }
        }

        return bitmap
    }
}
