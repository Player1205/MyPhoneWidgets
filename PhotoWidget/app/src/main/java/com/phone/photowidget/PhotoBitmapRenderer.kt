package com.phone.photowidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.exifinterface.media.ExifInterface
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
        borderStyle: String = PhotoManager.BORDER_GOLD_GLOW
    ): Bitmap {
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Adequate padding when glow is enabled so the soft ambient aura fades naturally into the wallpaper
        val padding = if (borderStyle == PhotoManager.BORDER_GOLD_GLOW) 24f else 6f
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
                // Default: Curved Rectangle filling the 2x2 widget area
                // Samsung One UI 28dp corner curvature (~17.5% ratio)
                val cornerRadius = min(rect.width(), rect.height()) * 0.175f
                shapePath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
            }
        }

        // STEP 1: Draw the ambient glow BEHIND the widget (subtle, soft golden aura)
        if (borderStyle == PhotoManager.BORDER_GOLD_GLOW) {
            drawSubtleGlowBehind(canvas, shapePath)
        }

        // STEP 2: Draw Photo or Placeholder
        if (photoFile != null && photoFile.exists()) {
            val sourceBitmap = decodeSampledBitmap(photoFile.absolutePath, width, height)
            if (sourceBitmap != null) {
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

        // STEP 3: Draw the Golden Border Rim on Top
        drawBorder(canvas, shapePath, rect, shape, borderStyle)

        return output
    }

    /**
     * Renders a soft, non-intrusive ambient golden glow emanating outwards behind the widget.
     * Controlled opacity ensures the glow is classy and not overpowering.
     */
    private fun drawSubtleGlowBehind(canvas: Canvas, path: Path) {
        // Outer diffuse ambient aura (soft Cyber Amber, ~16% opacity)
        val outerAuraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 16f
            color = Color.argb(42, 255, 183, 3) // Cyber Amber #FFB703
            maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
            isDither = true
        }
        canvas.drawPath(path, outerAuraPaint)

        // Inner tighter radiant halo (~30% opacity)
        val innerHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 9f
            color = Color.argb(78, 255, 191, 26) // Royal Honey Gold #FFBF1A
            maskFilter = BlurMaskFilter(7f, BlurMaskFilter.Blur.NORMAL)
            isDither = true
        }
        canvas.drawPath(path, innerHaloPaint)
    }

    private fun drawBorder(
        canvas: Canvas,
        path: Path,
        rect: RectF,
        shape: String,
        borderStyle: String
    ) {
        when (borderStyle) {
            PhotoManager.BORDER_GOLD_GLOW, PhotoManager.BORDER_GOLD -> {
                // Main Golden Stroke (Cyber Amber #FFB703)
                val goldBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 5.5f
                    color = Color.parseColor("#FFB703")
                }
                canvas.drawPath(path, goldBorderPaint)

                // Inner hairline golden highlight (#FFE58F champagne gold)
                val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1.6f
                    color = Color.parseColor("#FFE58F")
                }

                val inset = 1.8f
                val innerRect = RectF(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset)
                val innerPath = Path()
                when (shape) {
                    PhotoManager.SHAPE_OVAL -> innerPath.addOval(innerRect, Path.Direction.CW)
                    PhotoManager.SHAPE_CIRCLE -> {
                        val size = min(innerRect.width(), innerRect.height())
                        val cx = innerRect.centerX()
                        val cy = innerRect.centerY()
                        val r = size / 2f
                        innerPath.addOval(RectF(cx - r, cy - r, cx + r, cy + r), Path.Direction.CW)
                    }
                    else -> {
                        val cornerRadius = min(innerRect.width(), innerRect.height()) * 0.175f
                        innerPath.addRoundRect(innerRect, cornerRadius, cornerRadius, Path.Direction.CW)
                    }
                }
                canvas.drawPath(innerPath, highlightPaint)
            }
            PhotoManager.BORDER_DARK -> {
                val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 6f
                    color = Color.parseColor("#121520")
                }
                canvas.drawPath(path, rimPaint)

                val hairlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    color = Color.parseColor("#262D42")
                }
                canvas.drawPath(path, hairlinePaint)
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
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#121522")
        }
        canvas.drawPath(shapePath, bgPaint)

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
            val decoded = BitmapFactory.decodeFile(filePath, decodeOptions) ?: return null

            // Correct EXIF orientation so photos always render upright
            try {
                val exif = ExifInterface(filePath)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val rotationDegrees = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
                if (rotationDegrees != 0f) {
                    val matrix = Matrix().apply { postRotate(rotationDegrees) }
                    val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                    if (rotated != decoded) {
                        decoded.recycle()
                    }
                    rotated
                } else {
                    decoded
                }
            } catch (e: Exception) {
                e.printStackTrace()
                decoded
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
