package com.example.draganddraw

import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.view.drawToBitmap
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

val TAG = "BoxDrawing"

data class Figure(
    val originPosition: PointF,
    val type: Int,
    var prefs: Paint,
    var path: Path? = null,
    var fill: Bitmap? = null,
    var currentPosition: PointF = originPosition
)

class BoxDrawingView(context: Context, attrSet: AttributeSet? = null) : View(context, attrSet) {
    interface DrawingCallback {
        fun checkMenuAfterDrawing()
    }

    private val drawingCallback: DrawingCallback = context as DrawingCallback
    var backgroundBitmap: Bitmap? = null

    companion object {
        const val BRUSH = 0
        const val ERASER = 1
        const val CIRCLE = 2
        const val ELLIPSE = 3
        const val RECTANGLE = 4
        const val LINE = 5
        const val FILL = 6
        const val TEXT = 7
        var currentTool = BRUSH

        //    var addTextSize = 100f
        var brushSize = 50f
        var eraserSize = 50f

        // Brush settings
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
                    CIRCLE -> {
                        currentFigure = Figure(current, CIRCLE, paint)
                    }
                    ELLIPSE -> {
                        currentFigure = Figure(current, ELLIPSE, paint)
                    }
                    RECTANGLE -> {
                        currentFigure = Figure(current, RECTANGLE, paint)
                    }
                    LINE -> {
                        currentFigure = Figure(current, LINE, paint)
                    }
                    FILL -> {
                        val currentState = this.drawToBitmap()
                        val q = QueueLinearFloodFiller(currentState, currentState!!.getPixel(event.x.toInt(), event.y.toInt()), Color.rgb(brushRed, brushGreen, brushBlue))
                        q.setTolerance(100)
                        q.floodFill(event.x.toInt(), event.y.toInt())
                        currentFigure = Figure(current, FILL, Paint(), null, q.getImage())
                    }
                    TEXT -> {
                        val textPaint = Paint().apply {
                            color = Color.BLUE
                            style = Paint.Style.FILL
//                            strokeWidth = 1f
                            textSize = 100f
                        }
                        currentFigure = Figure(current, TEXT, textPaint)
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
                    Log.i(TAG, "Figures size\t\t\t ${figures.size}")
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
        canvas.drawColor(paintBackgroundColor)
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap!!, 0f, 0f, backgroundPaint)
        }

        for (figure in figures) {
            val left = Math.min(figure.originPosition.x, figure.currentPosition.x)
            val right = Math.max(figure.originPosition.x, figure.currentPosition.x)
            val top = Math.min(figure.originPosition.y, figure.currentPosition.y)
            val bottom = Math.max(figure.originPosition.y, figure.currentPosition.y)

            when (figure.type) {
                BRUSH -> {
                    canvas.drawPath(figure.path!!, figure.prefs)
                }
                ERASER -> {
                    canvas.drawPath(figure.path!!, figure.prefs)
                }
                CIRCLE -> {
                    val cx = figure.originPosition.x
                    val cy = figure.originPosition.y
                    val radius = Math.max(
                        Math.abs(figure.currentPosition.x - figure.originPosition.x),
                        Math.abs(figure.currentPosition.y - figure.originPosition.y)
                    )
                    canvas.drawCircle(cx, cy, radius, figure.prefs)
                }
                ELLIPSE -> {
                    canvas.drawOval(left, top, right, bottom, figure.prefs)
                }
                RECTANGLE -> {
                    canvas.drawRect(left, top, right, bottom, figure.prefs)
                }
                LINE -> {
                    canvas.drawLine(
                        figure.originPosition.x,
                        figure.originPosition.y,
                        figure.currentPosition.x,
                        figure.currentPosition.y,
                        figure.prefs
                    )
                }
                FILL -> {
                    canvas.drawBitmap(figure.fill!!, backgroundMatrix, backgroundPaint)
                }
                TEXT -> {
                    canvas.drawText("Текст", figure.currentPosition.x, figure.currentPosition.y, figure.prefs)
                }
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

    fun saveFile(): Boolean {
        return try {
            val path = File("${Environment.getExternalStorageDirectory()}/Pictures")
            val newFilePath = File(path, "Рисунок ${SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date())}.jpg")
            val out = FileOutputStream(newFilePath)

            this.drawToBitmap().compress(Bitmap.CompressFormat.JPEG, 95, out)

            Toast.makeText(
                context,
                "Рисунок ${SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date())} сохранён в ${Environment.getExternalStorageDirectory()}/Pictures",
                Toast.LENGTH_LONG
            ).show()
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(context, "Ошибка: ${e.printStackTrace()}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
