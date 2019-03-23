package com.example.draganddraw

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), BoxDrawingView.DrawingCallback {

    lateinit var menuUndo: MenuItem
    lateinit var menuRedo: MenuItem
    private val disabledAlpha = 100
    private val enabledAlpha = 255

    override fun checkMenuAfterDrawing() {
        menuUndo.apply {
            isEnabled = true
            icon.alpha = enabledAlpha
        }
        menuRedo.apply {
            isEnabled = false
            icon.alpha = disabledAlpha
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        save.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED

                ) {
                    if (boxdrawing.saveFile()) {
                        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Mmm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menuUndo = menu?.findItem(R.id.undo)!!
        menuRedo = menu.findItem(R.id.redo)!!
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.undo)?.apply {
            isEnabled = false
            icon.alpha = disabledAlpha
        }
        menu?.findItem(R.id.redo)?.apply {
            isEnabled = false
            icon.alpha = disabledAlpha
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.undo -> {
                menuUndo.apply {
                    isEnabled = boxdrawing.undo()
                    if (!isEnabled)
                        icon.alpha = disabledAlpha
                }
                menuRedo.apply {
                    isEnabled = true
                    icon.alpha = enabledAlpha
                }
            }
            R.id.redo -> {
                menuRedo.apply {
                        isEnabled = boxdrawing.redo()
                    if (!isEnabled)
                        icon.alpha = disabledAlpha
                    }
                menuUndo.apply {
                    isEnabled = true
                    icon.alpha = enabledAlpha
                }
            }
            R.id.brush -> {
                BoxDrawingView.currentTool = BoxDrawingView.BRUSH
                toast("Нарисовать на заборе")
            }
            R.id.eraser -> {
                BoxDrawingView.currentTool = BoxDrawingView.ERASER
                toast("Ластег")
            }
            R.id.circle -> {
                BoxDrawingView.currentTool = BoxDrawingView.CIRCLE
                toast("Михаил")
            }
            R.id.ellipse -> {
                BoxDrawingView.currentTool = BoxDrawingView.ELLIPSE
                toast("Недомихаил")
            }
            R.id.rectangle -> {
                BoxDrawingView.currentTool = BoxDrawingView.RECTANGLE
                toast("Прямоугольник")
            }
            R.id.line -> {
                BoxDrawingView.currentTool = BoxDrawingView.LINE
                toast("Линия партии")
            }
            R.id.background -> {
                BoxDrawingView.currentTool = BoxDrawingView.BACKGROUND
                toast("Выбрать забор")
            }
            R.id.text -> {
                BoxDrawingView.currentTool = BoxDrawingView.TEXT
                toast("Написать на заборе")
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
