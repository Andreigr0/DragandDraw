package com.example.draganddraw

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.isSelectable
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onFocusChange
import org.jetbrains.anko.sdk27.coroutines.onTouch
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

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
      redo.elevation = 5f

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.undo)?.isEnabled = boxdrawing.undo()
        menu?.findItem(R.id.redo)?.isEnabled = boxdrawing.redo()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.undo -> {
                item.isEnabled = boxdrawing.undo()
                invalidateOptionsMenu()
            }
            R.id.redo -> {
                item.isEnabled = boxdrawing.redo()
                invalidateOptionsMenu()
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
