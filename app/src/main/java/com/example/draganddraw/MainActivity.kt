package com.example.draganddraw

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity(), BoxDrawingView.DrawingCallback {
    private val disabledAlpha = 100
    private val enabledAlpha = 255

    private lateinit var tools: ArrayList<View>

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

        selectCurrentTool(button_brush)

        button_save.onClick {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    boxdrawing.saveFile()
            } else {
                Toast.makeText(this@MainActivity, "Mmm", Toast.LENGTH_SHORT).show()
            }
        }

        with(button_undo) {
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

        with(button_redo) {
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
            boxdrawing.createNew()
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
            selectCurrentTool(button_brush)
            BoxDrawingView.currentTool = BoxDrawingView.BRUSH
        }

        button_eraser.onClick {
            selectCurrentTool(button_eraser)
            BoxDrawingView.currentTool = BoxDrawingView.ERASER
        }

        button_circle.onClick {
            selectCurrentTool(button_circle)
            BoxDrawingView.currentTool = BoxDrawingView.CIRCLE
        }

        button_ellipse.onClick {
            selectCurrentTool(button_ellipse)
            BoxDrawingView.currentTool = BoxDrawingView.ELLIPSE
        }

        button_rectangle.onClick {
            selectCurrentTool(button_rectangle)
            BoxDrawingView.currentTool = BoxDrawingView.RECTANGLE
        }

        button_line.onClick {
            selectCurrentTool(button_line)
            BoxDrawingView.currentTool = BoxDrawingView.LINE
        }

        // disabled
        button_fill.onClick {
            //            toast("Выбор фона")
//            val bitmap: Bitmap = BitmapFactory.decodeStream(assets.open("image.jpg"))

//            selectCurrentTool(button_background)
            BoxDrawingView.currentTool = BoxDrawingView.FILL
        }

        // hide from layout
        button_text.visibility = View.GONE
//        button_background.visibility = View.GONE
        button_size.visibility = View.GONE

        button_text.onClick {
            selectCurrentTool(button_text)
            BoxDrawingView.currentTool = BoxDrawingView.TEXT
        }
        button_size.onClick { toast("Изменить размер изображения") }

    }


    private fun selectCurrentTool(view: View) {
        for (i in tools) {
            i.backgroundColorResource = R.color.colorPrimary
        }
        view.backgroundColorResource = R.color.colorPrimaryDark
    }

    override fun checkMenuAfterDrawing() {
        button_undo.isEnabled = true
        icon_undo.imageAlpha = enabledAlpha
        button_redo.isEnabled = false
        icon_redo.imageAlpha = disabledAlpha
    }
}