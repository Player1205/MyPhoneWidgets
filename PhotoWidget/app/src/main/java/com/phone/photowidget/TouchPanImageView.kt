package com.phone.photowidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

class TouchPanImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val matrix = Matrix()
    private val prevMatrix = Matrix()

    private var mode = NONE
    private val startPoint = PointF()
    private val midPoint = PointF()
    private var oldDist = 1f

    private var scaleDetector: ScaleGestureDetector? = null
    private var currentScale = 1f
    private val minScale = 1f
    private val maxScale = 4f

    private var bitmapWidth = 0f
    private var bitmapHeight = 0f

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    init {
        scaleType = ScaleType.MATRIX
        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor
                val newScale = currentScale * scaleFactor
                if (newScale in minScale..maxScale) {
                    currentScale = newScale
                    matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                    clampMatrix()
                    imageMatrix = matrix
                }
                return true
            }
        })
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        if (bm != null) {
            bitmapWidth = bm.width.toFloat()
            bitmapHeight = bm.height.toFloat()
            post { resetAndFitImage() }
        }
    }

    fun resetAndFitImage() {
        if (bitmapWidth <= 0f || bitmapHeight <= 0f || width <= 0 || height <= 0) return

        matrix.reset()
        val scale = max(width / bitmapWidth, height / bitmapHeight)
        currentScale = scale

        val dx = (width - bitmapWidth * scale) / 2f
        val dy = (height - bitmapHeight * scale) / 2f

        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
        imageMatrix = matrix
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector?.onTouchEvent(event)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                prevMatrix.set(matrix)
                startPoint.set(event.x, event.y)
                mode = DRAG
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    prevMatrix.set(matrix)
                    midPoint(midPoint, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    matrix.set(prevMatrix)
                    val dx = event.x - startPoint.x
                    val dy = event.y - startPoint.y
                    matrix.postTranslate(dx, dy)
                    clampMatrix()
                    imageMatrix = matrix
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    private fun clampMatrix() {
        val values = FloatArray(9)
        matrix.getValues(values)

        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]
        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]

        val scaledWidth = bitmapWidth * scaleX
        val scaledHeight = bitmapHeight * scaleY

        var deltaX = 0f
        var deltaY = 0f

        if (scaledWidth < width) {
            deltaX = (width - scaledWidth) / 2f - transX
        } else if (transX > 0) {
            deltaX = -transX
        } else if (transX + scaledWidth < width) {
            deltaX = width - (transX + scaledWidth)
        }

        if (scaledHeight < height) {
            deltaY = (height - scaledHeight) / 2f - transY
        } else if (transY > 0) {
            deltaY = -transY
        } else if (transY + scaledHeight < height) {
            deltaY = height - (transY + scaledHeight)
        }

        matrix.postTranslate(deltaX, deltaY)
    }

    /**
     * Calculates the normalized center point (0.0 to 1.0) of the visible area.
     * Used so the user can save their slid/panned position onto the widget!
     */
    fun getNormalizedCenterOffset(): Pair<Float, Float> {
        val values = FloatArray(9)
        matrix.getValues(values)

        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]
        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]

        val scaledW = bitmapWidth * scaleX
        val scaledH = bitmapHeight * scaleY

        val maxOffsetX = scaledW - width
        val maxOffsetY = scaledH - height

        val panX = if (maxOffsetX > 0) (-transX / maxOffsetX).coerceIn(0f, 1f) else 0.5f
        val panY = if (maxOffsetY > 0) (-transY / maxOffsetY).coerceIn(0f, 1f) else 0.5f

        return Pair(panX, panY)
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return kotlin.math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }
}
