package com.example.draganddraw

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_menu.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onSeekBarChangeListener
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.toast

class BottomMenu : BottomSheetDialogFragment() {
    interface BottomMenuCallback {
        fun adjustBrushSize(value: Int)
        fun adjustEraser(value: Int)
        fun adjustColor(alp: Int, red: Int, green: Int, blue: Int)
    }

    lateinit var callback: BottomMenuCallback
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = activity as BottomMenuCallback
    }

    var alp = 200
    var red = 200
    var green = 140
    var blue = 150
    val col = Color.argb(alp, red, green, blue).toDrawable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        brush_size.progress = (activity?.boxdrawing?.brushSize)!!.toInt()
        brush_size_text.setText(brush_size.progress.toString())

        eraser_size.progress = activity?.boxdrawing?.eraserSize!!.toInt()
        eraser_size_text.setText(eraser_size.progress.toString())

        alpha_value.progress = activity?.boxdrawing?.alp!!.toInt()
        alp = alpha_value.progress
        alpha_value_text.setText(alpha_value.progress.toString())

        red_value.progress = activity?.boxdrawing?.red!!.toInt()
        red = red_value.progress
        red_value_text.setText(red_value.progress.toString())

        green_value.progress = activity?.boxdrawing?.green!!.toInt()
        green = green_value.progress
        green_value_text.setText(green_value.progress.toString())

        blue_value.progress = activity?.boxdrawing?.blue!!.toInt()
        blue = blue_value.progress
        blue_value_text.setText(blue_value.progress.toString())
        brush_preview.setImageDrawable(col.apply { color = Color.argb(alp, red, green, blue) })

        brush_size.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b -> brush_size_text.setText(i.toString()) }
            onStopTrackingTouch { callback.adjustBrushSize(it?.progress!!) }
        }

        eraser_size.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b -> eraser_size_text.setText(i.toString()) }
            onStopTrackingTouch { callback.adjustEraser(it?.progress!!) }
        }

        alpha_value.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b ->
                alpha_value_text.setText(i.toString())
                alp = i
                brush_preview.setImageDrawable(col.apply { alpha = alp })
            }
            onStopTrackingTouch { callback.adjustColor(alp, red, green, blue) }
        }

        red_value.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b ->
                red_value_text.setText(i.toString())
                red = i
                brush_preview.setImageDrawable(col.apply { color = Color.argb(alp, red, green, blue) })
            }
            onStopTrackingTouch { callback.adjustColor(alp, red, green, blue) }
        }

        green_value.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b ->
                green_value_text.setText(i.toString())
                green = i
                brush_preview.setImageDrawable(col.apply { color = Color.argb(alp, red, green, blue) })
            }
            onStopTrackingTouch { callback.adjustColor(alp, red, green, blue) }
        }

        blue_value.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b ->
                blue_value_text.setText(i.toString())
                blue = i
                brush_preview.setImageDrawable(col.apply { color = Color.argb(alp, red, green, blue) })
            }
            onStopTrackingTouch { callback.adjustColor(alp, red, green, blue) }
        }


    }
}

class MainActivity : AppCompatActivity(), BoxDrawingView.DrawingCallback, BottomMenu.BottomMenuCallback {
    private val disabledAlpha = 100
    private val enabledAlpha = 255

    lateinit var tools: ArrayList<View>

    override fun adjustBrushSize(value: Int) {
        boxdrawing.brushSize = value.toFloat()
    }

    override fun adjustEraser(value: Int) {
        boxdrawing.eraserSize = value.toFloat()
    }

    override fun adjustColor(alp: Int, red: Int, green: Int, blue: Int) {
        Log.i(TAG, "Adjust color: Alpha: $alp Red: $red, Green: $green, Blue: $blue")
        boxdrawing.alp = alp
        boxdrawing.red = red
        boxdrawing.green = green
        boxdrawing.blue = blue
    }

    override fun checkMenuAfterDrawing() {
        button_undo.isEnabled = true
        icon_undo.imageAlpha = enabledAlpha
        button_redo.isEnabled = false
        icon_redo.imageAlpha = disabledAlpha
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tools = arrayListOf(
            button_brush,
            button_eraser,
            button_circle,
            button_ellipse,
            button_rectangle,
            button_line,
            button_text
        )

        currentTool(button_brush)

        button_save.onClick {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED

                ) {
                    if (boxdrawing.saveFile()) {
                        Toast.makeText(this@MainActivity, "Saved", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@MainActivity, "Mmm", Toast.LENGTH_SHORT).show()
            }
        }
        button_undo.apply {
            isEnabled = false
            icon_undo.imageAlpha = disabledAlpha
            onClick {
                button_undo.isEnabled = boxdrawing.undo()
                if (!button_undo.isEnabled) icon_undo.imageAlpha = disabledAlpha
                button_redo.isEnabled = true
                icon_redo.imageAlpha = enabledAlpha
            }
        }
        button_redo.apply {
            isEnabled = false
            icon_redo.imageAlpha = disabledAlpha
            onClick {
                button_redo.isEnabled = boxdrawing.redo()
                if (!button_redo.isEnabled) icon_redo.imageAlpha = disabledAlpha
                button_undo.isEnabled = true
                icon_undo.imageAlpha = enabledAlpha
            }
        }
        button_adjust.onClick {
            val bottomMenu = BottomMenu()
            bottomMenu.show(supportFragmentManager, "bottom_menu")
        }
        button_brush.onClick {
            currentTool(button_brush)
            BoxDrawingView.currentTool = BoxDrawingView.BRUSH
        }
        button_eraser.onClick {
            currentTool(button_eraser)
            BoxDrawingView.currentTool = BoxDrawingView.ERASER
        }
        button_circle.onClick {
            currentTool(button_circle)
            BoxDrawingView.currentTool = BoxDrawingView.CIRCLE
        }
        button_ellipse.onClick {
            currentTool(button_ellipse)
            BoxDrawingView.currentTool = BoxDrawingView.ELLIPSE
        }
        button_rectangle.onClick {
            currentTool(button_rectangle)
            BoxDrawingView.currentTool = BoxDrawingView.RECTANGLE
        }
        button_line.onClick {
            currentTool(button_line)
            BoxDrawingView.currentTool = BoxDrawingView.LINE
        }
        button_background.onClick { toast("Выбор фона") }
        button_text.onClick {
            currentTool(button_text)
            BoxDrawingView.currentTool = BoxDrawingView.TEXT
        }
        button_size.onClick { toast("Изменить размер изображения") }
    }

    private fun currentTool(view: View) {
        for (i in tools) {
            i.backgroundColor = 0x7700FF
        }
        view.setBackgroundColor(Color.RED)
    }
}
