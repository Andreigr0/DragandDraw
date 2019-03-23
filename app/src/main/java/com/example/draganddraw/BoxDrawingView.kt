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
import kotlin.random.Random

val TAG = "BoxDrawing"

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

    private val figures = ArrayList<Figure>()
    private val copy = ArrayList<Figure>()
    private var triggerUndo = true
    private lateinit var currentFigure: Figure

    private val mBackgroundPaint = Paint().apply { color = Color.YELLOW }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.i(TAG, "View touch")

        val current = PointF(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val randPaint = Paint().apply { strokeWidth = 20f; textSize = 100f; style = Paint.Style.STROKE }
                val r = Random
                val c = Color.argb(r.nextInt(0, 256), r.nextInt(0, 256), r.nextInt(0, 256), r.nextInt(0, 256))
                randPaint.color = c

                when (currentTool) {
                    BRUSH -> {
                        val path = Path()
                        path.moveTo(current.x, current.y)
                        currentFigure = Figure(current, BRUSH, randPaint, path)
                        figures.add(currentFigure)
                    }
                    ERASER -> {
                        val eraserPaint = Paint().apply {
                            color = mBackgroundPaint.color; style = Paint.Style.STROKE; strokeWidth = 100f
                        }
                        val path = Path()
                        path.moveTo(current.x, current.y)
                        currentFigure = Figure(current, ERASER, eraserPaint, path)
                        figures.add(currentFigure)
                    }
                    CIRCLE -> {
                        currentFigure = Figure(current, CIRCLE)
                        figures.add(currentFigure)
                    }
                    ELLIPSE -> {
                        currentFigure = Figure(current, ELLIPSE, randPaint)
                        figures.add(currentFigure)
                    }
                    RECTANGLE -> {
                        currentFigure = Figure(current, RECTANGLE, randPaint)
                        figures.add(currentFigure)
                    }
                    LINE -> {
                        currentFigure = Figure(current, LINE, randPaint)
                        figures.add(currentFigure)
                    }
                    BACKGROUND -> {
                    }
                    TEXT -> {
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
                    }
                }
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
        canvas.drawPaint(mBackgroundPaint)  // canvas.drawColor(Color.WHITE)
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
                    val cx = (figure.origin.x + figure.current.x) / 2
                    val cy = (figure.origin.y + figure.current.y) / 2
                    val radius = Math.max(
                        Math.abs((figure.current.x - figure.origin.x) / 2),
                        Math.abs((figure.current.y - figure.origin.y) / 2)
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
                    canvas.drawLine(figure.origin.x, figure.origin.y, figure.current.x, figure.current.y, figure.prefs)
                }
                BACKGROUND -> {
                }
                TEXT -> {
//                    canvas.drawText("Текст", 300f, 400f, paint)
                }
            }
        }
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
            Log.i(TAG, "---- ${copy.size}, ${figures.size}")
            return (copy.size > figures.size)
        }
        return false
    }

    fun saveFile(): Boolean {
        return try {
            val b = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            this.draw(c)

            val path = File(Environment.getExternalStorageDirectory().toString())
            val newFilePath =
                File(path, "Запись на заборе от ${SimpleDateFormat("dd-MM-yyyy HH:mm").format(Date())}.jpg")

            val out = FileOutputStream(newFilePath)
            b.compress(Bitmap.CompressFormat.JPEG, 95, out)
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        }
    }
}
