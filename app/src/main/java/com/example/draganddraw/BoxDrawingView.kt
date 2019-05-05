package com.example.draganddraw

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.drawToBitmap
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val TAG = "BoxDrawing"

data class Figure(val originPosition: PointF, val type: Int, var prefs: Paint, var path: Path? = null, var fill: Bitmap? = null, var currentPosition: PointF = originPosition)

class BoxDrawingView(context: Context, attrSet: AttributeSet? = null) : ImageView(context, attrSet) {
    interface DrawingCallback {
        fun checkMenuAfterDrawing()
    }

    private val drawingCallback: DrawingCallback = context as DrawingCallback
    private var backgroundBitmap: Bitmap? = null

    companion object {
        const val BRUSH = 0
        const val ERASER = 1
        const val CIRCLE = 2
        const val ELLIPSE = 3
        const val RECTANGLE = 4
        const val LINE = 5
        const val FILL = 6
        var currentTool = BRUSH

        var brushSize = 50f
        var eraserSize = 120f

        var brushAlpha = 255
        var brushRed = 0
        var brushGreen = 0
        var brushBlue = 0

        var paintBackgroundColor = Color.WHITE
        val backgroundPaint = Paint()
        val backgroundMatrix = Matrix()
    }

    private val figures = ArrayList<Figure>()
    private val copy = ArrayList<Figure>()
    private var triggerUndo = true
    private lateinit var currentFigure: Figure

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val current = PointF(event.x, event.y)
        val paint = Paint().apply {
            strokeWidth = brushSize
            style = Paint.Style.STROKE
            color = Color.argb(brushAlpha, brushRed, brushGreen, brushBlue)
            isAntiAlias = true
            isDither = true
            xfermode = null
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (currentTool) {
                    BRUSH -> {
                        paint.apply {
                            strokeJoin = Paint.Join.ROUND
                            strokeCap = Paint.Cap.ROUND
                        }
                        val path = Path()
                        path.moveTo(current.x, current.y)
                        currentFigure = Figure(current, BRUSH, paint, path)
                    }
                    ERASER -> {
                        val path = Path()
                        path.moveTo(current.x, current.y)
                        currentFigure = Figure(current, ERASER, Paint().apply {
                            color = paintBackgroundColor
                            style = Paint.Style.STROKE
                            strokeWidth = eraserSize
                            strokeJoin = Paint.Join.ROUND
                            strokeCap = Paint.Cap.ROUND
                        }, path)
                    }
                    CIRCLE -> currentFigure = Figure(current, CIRCLE, paint)
                    ELLIPSE -> currentFigure = Figure(current, ELLIPSE, paint)
                    RECTANGLE -> currentFigure = Figure(current, RECTANGLE, paint)
                    LINE -> currentFigure = Figure(current, LINE, paint)
                    FILL -> {
                        val currentState = this.drawToBitmap()
                        val q = QueueLinearFloodFiller(currentState, currentState.getPixel(event.x.toInt(), event.y.toInt()), Color.argb(brushAlpha, brushRed, brushGreen, brushBlue))
                        q.setTolerance(30)
                        q.floodFill(event.x.toInt(), event.y.toInt())
                        currentFigure = Figure(current, FILL, Paint(), null, q.getImage())
                    }
                }

                if (figures.size < 30) {
                    figures.add(currentFigure)
                    Log.i(TAG, "Figures (${figures.size})")
                } else {
                    val start = copy.size
                    copy.addAll(figures)
                    figures.clear()
                    figures.add(copy[0])
                    backgroundBitmap = this.drawToBitmap()
                    figures.removeAt(0)
                    copy.removeAt(0)
                    val end = copy.size
                    for (i in start until end)
                        figures.add(copy[i])
                    figures.add(currentFigure)
                    Log.i(TAG, "Figures (${figures.size})")
                }
                invalidate()
                drawingCallback.checkMenuAfterDrawing()
            }
            MotionEvent.ACTION_MOVE -> {
                currentFigure.path?.quadTo(
                    currentFigure.currentPosition.x,
                    currentFigure.currentPosition.y,
                    (current.x + currentFigure.currentPosition.x) / 2,
                    (current.y + currentFigure.currentPosition.y) / 2
                )
                currentFigure.currentPosition = current
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                copy.clear()
                triggerUndo = true
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(paintBackgroundColor)
        if (backgroundBitmap != null) canvas.drawBitmap(backgroundBitmap!!, 0f, 0f, backgroundPaint)

        for (figure in figures) {
            val left = figure.originPosition.x
            val right = figure.currentPosition.x
            val top = figure.originPosition.y
            val bottom = figure.currentPosition.y

            when (figure.type) {
                BRUSH -> canvas.drawPath(figure.path!!, figure.prefs)
                ERASER -> canvas.drawPath(figure.path!!, figure.prefs)
                CIRCLE -> {
                    val dx = right - left
                    val dy = bottom - top
                    when {
                        dx > dy && dx > 0 && dy > 0 -> canvas.drawOval(left, top, right, top + dx, figure.prefs)
                        dx < dy && dx > 0 && dy > 0 -> canvas.drawOval(left, top, left + dy, bottom, figure.prefs)
                        Math.abs(dx) > dy && dx < 0 && dy > 0 -> canvas.drawOval(left, top, right, top + Math.abs(dx), figure.prefs)
                        Math.abs(dx) < dy && dx < 0 && dy > 0 -> canvas.drawOval(left, top, left - dy, bottom, figure.prefs)
                        dx < Math.abs(dy) && dx > 0 && dy < 0 -> canvas.drawOval(left, top, left + Math.abs(dy), bottom, figure.prefs)
                        dx > dy && dx > 0 && dy < 0 -> canvas.drawOval(left, top, right, top - dx, figure.prefs)
                        dx < dy && dx < 0 && dy < 0 -> canvas.drawOval(left, top, right, top + dx, figure.prefs)
                        dy < dx && dx < 0 && dy < 0 -> canvas.drawOval(left, top, left + dy, bottom, figure.prefs)
                    }
                }
                ELLIPSE -> canvas.drawOval(left, top, right, bottom, figure.prefs)
                RECTANGLE -> canvas.drawRect(left, top, right, bottom, figure.prefs)
                LINE -> canvas.drawLine(figure.originPosition.x, figure.originPosition.y, figure.currentPosition.x, figure.currentPosition.y, figure.prefs)
                FILL -> canvas.drawBitmap(figure.fill!!, backgroundMatrix, backgroundPaint)
            }
        }
    }


    fun createNew() {
        figures.clear()
        copy.clear()
        backgroundBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        invalidate()
    }

    fun undo(): Boolean {
        if (triggerUndo) {
            copy.addAll(figures)
            triggerUndo = false
        }

        if (figures.size != 0) {
            figures.removeAt(figures.size - 1)
            invalidate()
        }
        return (figures.size != 0)
    }

    fun redo(): Boolean {
        if (copy.size != 0 && copy.size != figures.size) {
            figures.add(copy[figures.size])
            invalidate()
        }
        return (copy.size > figures.size)
    }

    @SuppressLint("SimpleDateFormat")
    fun saveFile(): Boolean {
        return try {
            val pictureName = resources.getString(R.string.picture_name, SimpleDateFormat(resources.getString(R.string.date_format)).format(Date()))

            val path = File("${Environment.getExternalStorageDirectory()}/Pictures")
            val newFilePath = File(path, "$pictureName.jpg")
            val out = FileOutputStream(newFilePath)
            this.drawToBitmap().compress(Bitmap.CompressFormat.JPEG, 98, out)

            val saveResult = resources.getString(R.string.save_result, pictureName, "$path")
            Toast.makeText(context, saveResult, Toast.LENGTH_LONG).show()
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(context, resources.getString(R.string.save_error, e.printStackTrace()), Toast.LENGTH_SHORT).show()
            false
        }
    }
}
