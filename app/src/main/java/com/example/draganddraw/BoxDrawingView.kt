package com.example.draganddraw

import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

val TAG = "BoxDrawing"
// Andrey

data class Figure(
    val origin: PointF, val type: Int = 0, var prefs: Paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 20f
        style = Paint.Style.STROKE
    }, var path: Path? = null, var current: PointF = origin
)


class BoxDrawingView(context: Context, attrSet: AttributeSet? = null) : View(context, attrSet) {
    interface DrawingCallback {
        fun checkMenuAfterDrawing()
    }

    private val drawingCallback: DrawingCallback = context as DrawingCallback

    companion object {
        val BRUSH = 0
        val ERASER = 1
        val CIRCLE = 2
        val ELLIPSE = 3
        val RECTANGLE = 4
        val LINE = 5
        val BACKGROUND = 6
        val TEXT = 7
        var currentTool = BRUSH
    }

    var brushSize = 50f
    var eraserSize = 50f
    var addTextSize = 100f
    var paintBackgroundColor = Color.WHITE
//    var backgroundBitmap: Bitmap? = null
    var backgroundBitmap: Bitmap? =  Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
    var bitmapCanvasSize: Bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)


    var alpha = 255
    var red = 0
    var green = 0
    var blue = 135

    private fun adjustPaint(alpha: Int, red: Int, green: Int, blue: Int) = Paint().apply {
        strokeWidth = brushSize
        style = Paint.Style.STROKE
        color = Color.argb(alpha, red, green, blue)

        isAntiAlias = true
        isDither = true
        xfermode = null
    }

    var paint = adjustPaint(alpha, red, green, blue)

    fun createNew(color: Int) {
        figures.clear()
        copy.clear()
        backgroundBitmap = null
        paintBackgroundColor = color
        invalidate()
    }

    fun setBackground(bitmap: Bitmap) {
        Log.i(TAG, "Bitmap boxdraw $bitmap")
        backgroundBitmap = bitmap
//        bitmapCanvasSize = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
        invalidate()
    }

    private val figures = ArrayList<Figure>()
    private val copy = ArrayList<Figure>()
    private var triggerUndo = true
    private lateinit var currentFigure: Figure


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val current = PointF(event.x, event.y)

        paint = adjustPaint(alpha, red, green, blue)

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
                        figures.add(currentFigure)
                    }
                    ERASER -> {
                        val eraserPaint = Paint().apply {
                            color = paintBackgroundColor
                            style = Paint.Style.STROKE
                            strokeWidth = eraserSize
                            strokeJoin = Paint.Join.ROUND
                            strokeCap = Paint.Cap.ROUND
                        }
                        val path = Path()
                        path.moveTo(current.x, current.y)
                        currentFigure = Figure(current, ERASER, eraserPaint, path)
                        figures.add(currentFigure)
                    }
                    CIRCLE -> {
                        currentFigure = Figure(current, CIRCLE, paint)
                        figures.add(currentFigure)
                    }
                    ELLIPSE -> {
                        currentFigure = Figure(current, ELLIPSE, paint)
                        figures.add(currentFigure)
                    }
                    RECTANGLE -> {
                        currentFigure = Figure(current, RECTANGLE, paint)
                        figures.add(currentFigure)
                    }
                    LINE -> {
                        currentFigure = Figure(current, LINE, paint)
                        figures.add(currentFigure)
                    }
                    BACKGROUND -> {
                    }
                    TEXT -> {
                       val textPaint = Paint().apply {
                            color = Color.BLUE
                            style = Paint.Style.FILL
//                            strokeWidth = 1f
                            textSize = 100f
                        }
                        currentFigure = Figure(current, TEXT, textPaint)
                        figures.add(currentFigure)
                    }
                }
                drawingCallback.checkMenuAfterDrawing()
            }
            MotionEvent.ACTION_MOVE -> {
                when (currentTool) {
                    BRUSH -> {
                        currentFigure.path?.quadTo(
                            currentFigure.current.x,
                            currentFigure.current.y,
                            (current.x + currentFigure.current.x) / 2,
                            (current.y + currentFigure.current.y) / 2
                        )
                        currentFigure.current = current
                    }
                    ERASER -> {
                        currentFigure.path?.quadTo(
                            currentFigure.current.x,
                            currentFigure.current.y,
                            (current.x + currentFigure.current.x) / 2,
                            (current.y + currentFigure.current.y) / 2
                        )
                        currentFigure.current = current
                    }
                    CIRCLE -> {
                        currentFigure.current = current
                    }
                    ELLIPSE -> {
                        currentFigure.current = current
                    }
                    RECTANGLE -> {
                        currentFigure.current = current
                    }
                    LINE -> {
                        currentFigure.current = current
                    }
                    BACKGROUND -> {
                    }
                    TEXT -> {
                        currentFigure.current = current
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                when (currentTool) {
                    BRUSH -> {
                        currentFigure.path?.moveTo(current.x, current.y)
                    }
                }
                copy.clear()
                triggerUndo = true
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
//        canvas.save()
        if (backgroundBitmap != null) {
            canvas.drawColor(paintBackgroundColor)
            canvas.drawBitmap(backgroundBitmap!!, 0f, 0f, paint)
        } else {
            canvas.drawColor(paintBackgroundColor)
        }

        for (figure in figures) {
            val left = Math.min(figure.origin.x, figure.current.x)
            val right = Math.max(figure.origin.x, figure.current.x)
            val top = Math.min(figure.origin.y, figure.current.y)
            val bottom = Math.max(figure.origin.y, figure.current.y)

            when (figure.type) {
                BRUSH -> {
                    canvas.drawPath(figure.path!!, figure.prefs)
                }
                ERASER -> {
                    canvas.drawPath(figure.path!!, figure.prefs)
                }
                CIRCLE -> {
//                    val cx = figure.origin.x
//                    val cy = figure.origin.y
//                    val radius = Math.max(Math.abs(figure.current.x - figure.origin.x), Math.abs(figure.current.y - figure.origin.y))
//
                    val cx = (figure.origin.x + figure.current.x) / 2
                    val cy = (figure.origin.y + figure.current.y) / 2
                    val radius = Math.max(
                        Math.abs((figure.current.x - figure.origin.x) / 2),
                        Math.abs((figure.current.y - figure.origin.y) / 2)
                    )
                    canvas.drawCircle(cx, cy, radius, figure.prefs)
//                    canvas.drawCircle(100F, 100F, 100F, figure.prefs)
                }
                ELLIPSE -> {
                    canvas.drawOval(left, top, right, bottom, figure.prefs)
                }
                RECTANGLE -> {
                    canvas.drawRect(left, top, right, bottom, figure.prefs)
                }
                LINE -> {
                    canvas.drawLine(figure.origin.x, figure.origin.y, figure.current.x, figure.current.y, figure.prefs)
                }
                BACKGROUND -> {
                }
                TEXT -> {
                    canvas.drawText("Текст", figure.current.x, figure.current.y, figure.prefs)
                }
            }
        }
//        canvas.restore()
    }


    fun undo(): Boolean {
        if (triggerUndo) {
            copy.addAll(figures)
            triggerUndo = false
        }
        if (figures.size != 0) {
            figures.removeAt(figures.size - 1)
            invalidate()
            return (figures.size != 0)
        }
        return false
    }

    fun redo(): Boolean {
        if (copy.size != 0 && copy.size != figures.size) {
            figures.add(copy[figures.size])
            invalidate()
            return (copy.size > figures.size)
        }
        return false
    }

    fun saveFile(): Boolean {
        return try {
            val b = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            this.draw(c)

            val path = File("${Environment.getExternalStorageDirectory()}/Pictures")
            val newFilePath = File(path, "Рисунок ${SimpleDateFormat("dd-MM-yyyy HH").format(Date())}.jpg")

            val out = FileOutputStream(newFilePath)
            b.compress(Bitmap.CompressFormat.JPEG, 95, out)
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        }
    }
}
