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
     * Generates a complete 365-day ambient starry grid with tight, borderless text scrims:
     * - Background scrims are strictly limited to the text boundaries (no excess width).
     * - ZERO borderlines on the text backgrounds.
     * - Text "in 2026" is centered inside its capsule.
     * - Ambient dots are more visible and radiant throughout.
     * - Today's dot is drawn on the top layer at 100% full brightness (Cyber Amber rim & dynamic white sweep fill).
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

        // Tight text-only plinth bounding boxes (strictly limited to text, perfectly centered)
        val plinthYear = RectF(32f, 22f, 138f, 58f)
        val plinthHero = RectF(78f, 90f, 642f, 206f)
        val plinthQuote = RectF(155f, 242f, 565f, 326f)

        fun inAnyPlinth(x: Float, y: Float): Boolean {
            return plinthYear.contains(x, y) || plinthHero.contains(x, y) || plinthQuote.contains(x, y)
        }

        // Higher-visibility starlight dots outside text backgrounds
        val pastPaintOutside = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(72, 255, 255, 255) // More visible starlight (~28% opacity)
            style = Paint.Style.FILL
        }
        val futurePaintOutside = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(40, 255, 255, 255) // More visible hollow ring (~16% opacity)
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
        }

        // Softer dots inside text backgrounds
        val pastPaintInside = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(36, 255, 255, 255) // Visible, but softer (~14% opacity)
            style = Paint.Style.FILL
        }
        val futurePaintInside = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(20, 255, 255, 255) // Subtle hollow ring (~8% opacity)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }

        // Borderless dark glass fill paint (NO BORDERLINE!)
        val plinthFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(135, 8, 10, 18) // Soft translucent dark tint
            style = Paint.Style.FILL
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
            color = Color.argb(110, 255, 183, 3)
            style = Paint.Style.STROKE
            strokeWidth = 2.0f
        }

        // Step 1: Draw ambient dots across the entire grid
        var todayX = -1f
        var todayY = -1f

        var day = 1
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (day > totalDays) break
                val cx = paddingX + c * stepX
                val cy = paddingY + r * stepY

                val isInside = inAnyPlinth(cx, cy)

                if (day == dayOfYear) {
                    todayX = cx
                    todayY = cy
                } else if (day < dayOfYear) {
                    val paint = if (isInside) pastPaintInside else pastPaintOutside
                    canvas.drawCircle(cx, cy, 2.8f, paint)
                } else {
                    val paint = if (isInside) futurePaintInside else futurePaintOutside
                    canvas.drawCircle(cx, cy, 2.4f, paint)
                }
                day++
            }
        }

        // Step 2: Draw the borderless translucent text plinths (NO STROKE / NO BORDERLINE)
        canvas.drawRoundRect(plinthYear, 18f, 18f, plinthFillPaint)
        canvas.drawRoundRect(plinthHero, 24f, 24f, plinthFillPaint)
        canvas.drawRoundRect(plinthQuote, 20f, 20f, plinthFillPaint)

        // Step 3: Draw Today's Dot ON TOP (100% visible, bright Cyber Amber)
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
