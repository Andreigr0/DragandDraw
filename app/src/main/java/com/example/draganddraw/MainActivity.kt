package com.example.draganddraw

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast


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
        boxdrawing.alpha = alp
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
                if (!button_undo.isEnabled)
                    icon_undo.imageAlpha = disabledAlpha
                button_redo.isEnabled = true
                icon_redo.imageAlpha = enabledAlpha
            }
        }
        button_redo.apply {
            isEnabled = false
            icon_redo.imageAlpha = disabledAlpha
            onClick {
                button_redo.isEnabled = boxdrawing.redo()
                if (!button_redo.isEnabled)
                    icon_redo.imageAlpha = disabledAlpha
                button_undo.isEnabled = true
                icon_undo.imageAlpha = enabledAlpha
            }
        }

        button_new.onClick {
            boxdrawing.createNew(Color.YELLOW)
            button_undo.isEnabled = false
            button_redo.isEnabled = false
            icon_undo.imageAlpha = disabledAlpha
            icon_redo.imageAlpha = disabledAlpha
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

        button_background.onClick {
            toast("Выбор фона")
            val bitmap: Bitmap = BitmapFactory.decodeStream(assets.open("image.jpg"))

            Log.i(TAG, "$bitmap")
            boxdrawing.setBackground(bitmap)
        }

        // hide from layout
        button_text.visibility = View.GONE
        button_background.visibility = View.GONE
        button_size.visibility = View.GONE

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
        view.setBackgroundColor(Color.BLUE)
    }
}
