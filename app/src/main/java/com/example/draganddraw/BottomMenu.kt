package com.example.draganddraw

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_menu.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onSeekBarChangeListener
import yuku.ambilwarna.AmbilWarnaDialog

class BottomMenu : BottomSheetDialogFragment() {
    private fun preview() = Color.argb(BoxDrawingView.brushAlpha, BoxDrawingView.brushRed, BoxDrawingView.brushGreen, BoxDrawingView.brushBlue).toDrawable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fun adjustBrush() {
            brush_size.progress = BoxDrawingView.brushSize.toInt()
            eraser_size.progress = BoxDrawingView.eraserSize.toInt()
            alpha_value.progress = BoxDrawingView.brushAlpha
            red_value.progress = BoxDrawingView.brushRed
            green_value.progress = BoxDrawingView.brushGreen
            blue_value.progress = BoxDrawingView.brushBlue

            brush_size_text.text = brush_size.progress.toString()
            eraser_size_text.text = eraser_size.progress.toString()
            alpha_value_text.text = BoxDrawingView.brushAlpha.toString()
            red_value_text.text = BoxDrawingView.brushRed.toString()
            green_value_text.text = BoxDrawingView.brushGreen.toString()
            blue_value_text.text = BoxDrawingView.brushBlue.toString()

            brush_preview.setImageDrawable(preview())
        }

        brush_preview.onClick {
            val colorPicker = AmbilWarnaDialog(context, Color.argb(BoxDrawingView.brushAlpha, BoxDrawingView.brushRed, BoxDrawingView.brushGreen, BoxDrawingView.brushBlue), true, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                    override fun onCancel(dialog: AmbilWarnaDialog?) {

                    }

                    override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                        BoxDrawingView.brushAlpha = Color.alpha(color)
                        BoxDrawingView.brushRed = Color.red(color)
                        BoxDrawingView.brushGreen = Color.green(color)
                        BoxDrawingView.brushBlue = Color.blue(color)
                        adjustBrush()
                    }
                })
            colorPicker.show()
        }

        adjustBrush()

        brush_size.onSeekBarChangeListener {
            onProgressChanged { _, i, _ ->
                brush_size_text.text = i.toString()
                BoxDrawingView.brushSize = i.toFloat()
            }
        }

        eraser_size.onSeekBarChangeListener {
            onProgressChanged { _, i, _ ->
                eraser_size_text.text = i.toString()
                BoxDrawingView.eraserSize = i.toFloat()
            }
        }

        alpha_value.onSeekBarChangeListener {
            onProgressChanged { _, i, _ ->
                alpha_value_text.text = i.toString()
                brush_preview.setImageDrawable(preview())
                BoxDrawingView.brushAlpha = i
            }
        }

        red_value.onSeekBarChangeListener {
            onProgressChanged { _, i, _ ->
                red_value_text.text = i.toString()
                brush_preview.setImageDrawable(preview())
                BoxDrawingView.brushRed = i
            }
        }

        green_value.onSeekBarChangeListener {
            onProgressChanged { _, i, _ ->
                green_value_text.text = i.toString()
                brush_preview.setImageDrawable(preview())
                BoxDrawingView.brushGreen = i
            }
        }

        blue_value.onSeekBarChangeListener {
            onProgressChanged { _, i, _ ->
                blue_value_text.text = i.toString()
                brush_preview.setImageDrawable(preview())
                BoxDrawingView.brushBlue = i
            }
        }
    }
}