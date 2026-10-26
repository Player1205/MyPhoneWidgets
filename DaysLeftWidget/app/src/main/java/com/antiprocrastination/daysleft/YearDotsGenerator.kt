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
     * Generates a complete 365-day ambient starry grid with translucent text plinths:
     * - Dots inside the text backgrounds are visible, but softer/dimmed for perfect text contrast.
     * - Dots outside the text backgrounds maintain standard starlight visibility.
     * - TODAY'S DOT is drawn on top with 100% full brightness (Cyber Amber rim & dynamic white sweep fill).
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

        // Text plinths bounding zones
        val plinthYear = RectF(34f, 22f, 160f, 62f)
        val plinthHero = RectF(50f, 86f, 670f, 212f)
        val plinthQuote = RectF(150f, 238f, 570f, 328f)

        fun inAnyPlinth(x: Float, y: Float): Boolean {
            return plinthYear.contains(x, y) || plinthHero.contains(x, y) || plinthQuote.contains(x, y)
        }

        // Paints for dots outside the text backgrounds (normal starlight)
        val pastPaintOutside = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(48, 255, 255, 255)
            style = Paint.Style.FILL
        }
        val futurePaintOutside = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(26, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
        }

        // Paints for dots inside the text backgrounds (visible, but softer/dimmer)
        val pastPaintInside = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(24, 255, 255, 255)
            style = Paint.Style.FILL
        }
        val futurePaintInside = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(14, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }

        // Translucent Plinth Paints (frosted dark obsidian glass)
        val plinthFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(140, 8, 10, 18) // ~55% dark glass
            style = Paint.Style.FILL
        }
        val plinthStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(20, 255, 255, 255) // subtle 1px border
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
        }

        // Today's dot paints (100% bright, full intensity)
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
            color = Color.argb(100, 255, 183, 3)
            style = Paint.Style.STROKE
            strokeWidth = 2.0f
        }

        // Step 1: Draw ambient dots
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
                    canvas.drawCircle(cx, cy, 2.6f, paint)
                } else {
                    val paint = if (isInside) futurePaintInside else futurePaintOutside
                    canvas.drawCircle(cx, cy, 2.2f, paint)
                }
                day++
            }
        }

        // Step 2: Draw the translucent plinths
        canvas.drawRoundRect(plinthYear, 18f, 18f, plinthFillPaint)
        canvas.drawRoundRect(plinthYear, 18f, 18f, plinthStrokePaint)

        canvas.drawRoundRect(plinthHero, 26f, 26f, plinthFillPaint)
        canvas.drawRoundRect(plinthHero, 26f, 26f, plinthStrokePaint)

        canvas.drawRoundRect(plinthQuote, 20f, 20f, plinthFillPaint)
        canvas.drawRoundRect(plinthQuote, 20f, 20f, plinthStrokePaint)

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
