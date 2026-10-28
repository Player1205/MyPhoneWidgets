package com.phone.photowidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import java.io.File
import kotlin.math.max
import kotlin.math.min

object PhotoBitmapRenderer {

    fun renderWidgetBitmap(
        context: Context,
        photoFile: File?,
        width: Int = 720,
        height: Int = 720,
        shape: String = PhotoManager.SHAPE_ROUNDED,
        borderStyle: String = PhotoManager.BORDER_DARK
    ): Bitmap {
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Minimal padding to prevent anti-aliased edge clipping while filling 100% of widget area
        val strokeWidth = 8f
        val padding = strokeWidth / 2f
        val rect = RectF(padding, padding, width - padding, height - padding)

        val shapePath = Path()
        when (shape) {
            PhotoManager.SHAPE_OVAL -> {
                shapePath.addOval(rect, Path.Direction.CW)
            }
            PhotoManager.SHAPE_CIRCLE -> {
                val size = min(rect.width(), rect.height())
                val cx = rect.centerX()
                val cy = rect.centerY()
                val r = size / 2f
                val circleRect = RectF(cx - r, cy - r, cx + r, cy + r)
                shapePath.addOval(circleRect, Path.Direction.CW)
            }
            else -> {
                // Default: Curved Rectangle filling the full 2x2 widget area
                // Corner radius mathematically matches Samsung One UI standard 28dp (~17.5% of dimension)
                val cornerRadius = min(rect.width(), rect.height()) * 0.175f
                shapePath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
            }
        }

        if (photoFile != null && photoFile.exists()) {
            val sourceBitmap = decodeSampledBitmap(photoFile.absolutePath, width, height)
            if (sourceBitmap != null) {
                // Center-crop shader
                val shader = BitmapShader(sourceBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                val matrix = Matrix()

                val bWidth = sourceBitmap.width.toFloat()
                val bHeight = sourceBitmap.height.toFloat()

                val scale = max(rect.width() / bWidth, rect.height() / bHeight)
                val dx = rect.left + (rect.width() - bWidth * scale) / 2f
                val dy = rect.top + (rect.height() - bHeight * scale) / 2f

                matrix.setScale(scale, scale)
                matrix.postTranslate(dx, dy)
                shader.setLocalMatrix(matrix)

                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    isFilterBitmap = true
                    this.shader = shader
                }

                canvas.drawPath(shapePath, fillPaint)
                sourceBitmap.recycle()
            } else {
                drawPlaceholder(canvas, rect, shapePath)
            }
        } else {
            drawPlaceholder(canvas, rect, shapePath)
        }

        // Draw elegant rim borders
        drawBorder(canvas, shapePath, borderStyle, strokeWidth)

        return output
    }

    private fun drawBorder(
        canvas: Canvas,
        path: Path,
        borderStyle: String,
        strokeWidth: Float
    ) {
        when (borderStyle) {
            PhotoManager.BORDER_DARK -> {
                // Outer dark obsidian rim (Matches user uploaded photo oval frame)
                val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    this.strokeWidth = strokeWidth
                    color = Color.parseColor("#0E1017")
                }
                canvas.drawPath(path, rimPaint)

                // Subtle inner accent hairline for high-DPI depth definition
                val hairlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    this.strokeWidth = 2f
                    color = Color.parseColor("#262D42")
                }
                canvas.drawPath(path, hairlinePaint)
            }
            PhotoManager.BORDER_AMBER -> {
                val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    this.strokeWidth = strokeWidth * 0.8f
                    color = Color.parseColor("#FFB703")
                }
                canvas.drawPath(path, rimPaint)
            }
            PhotoManager.BORDER_NONE -> {
                // Pure clean cutout without border
            }
        }
    }

    private fun drawPlaceholder(
        canvas: Canvas,
        rect: RectF,
        shapePath: Path
    ) {
        // Dark placeholder surface
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#121522")
        }
        canvas.drawPath(shapePath, bgPaint)

        // Accent plus icon and label in center
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFB703")
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9DA4B4")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFB703")
            textSize = 72f
            textAlign = Paint.Align.CENTER
        }

        val cy = rect.centerY()
        canvas.drawText("＋", rect.centerX(), cy - 20f, iconPaint)
        canvas.drawText("Add Photos", rect.centerX(), cy + 40f, textPaint)
        canvas.drawText("Tap to setup widget", rect.centerX(), cy + 80f, subPaint)
    }

    private fun decodeSampledBitmap(
        filePath: String,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, options)

            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(filePath, decodeOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
