package com.example.draganddraw

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_menu.*
import org.jetbrains.anko.sdk27.coroutines.onSeekBarChangeListener

class BottomMenu : BottomSheetDialogFragment() {
    interface BottomMenuCallback {
        fun adjustBrushSize(value: Int)
        fun adjustEraser(value: Int)
        fun adjustColor(alp: Int, red: Int, green: Int, blue: Int)
    }

    private lateinit var callback: BottomMenuCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = activity as BottomMenuCallback
    }

    var alpha = 200
    var red = 200
    var green = 140
    var blue = 150
    val col = Color.argb(alpha, red, green, blue).toDrawable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Don't display keyboard automatically
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        // Brush size
        brush_size.progress = (activity?.boxdrawing?.brushSize)!!.toInt()
        brush_size_text.setText(brush_size.progress.toString())

        brush_size.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b -> brush_size_text.setText(i.toString()) }
            onStopTrackingTouch { callback.adjustBrushSize(it?.progress!!) }
        }

        // Eraser size
        eraser_size.progress = activity?.boxdrawing?.eraserSize!!.toInt()
        eraser_size_text.setText(eraser_size.progress.toString())

        eraser_size.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b -> eraser_size_text.setText(i.toString()) }
            onStopTrackingTouch { callback.adjustEraser(it?.progress!!) }
        }

        // Alpha channel
        alpha_value.progress = activity?.boxdrawing?.alpha!!.toInt()
        alpha = alpha_value.progress
        alpha_value_text.setText(alpha_value.progress.toString())

        alpha_value.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b ->
                alpha_value_text.setText(i.toString())
                alpha = i
                brush_preview.setImageDrawable(col.apply { alpha = alpha })
            }
            onStopTrackingTouch { callback.adjustColor(alpha, red, green, blue) }
        }

        // Red channel
        red_value.progress = activity?.boxdrawing?.red!!.toInt()
        red = red_value.progress
        red_value_text.setText(red_value.progress.toString())

        red_value.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b ->
                red_value_text.setText(i.toString())
                red = i
                brush_preview.setImageDrawable(col.apply { color = Color.argb(alpha, red, green, blue) })
            }
            onStopTrackingTouch { callback.adjustColor(alpha, red, green, blue) }
        }

        // Green channel
        green_value.progress = activity?.boxdrawing?.green!!.toInt()
        green = green_value.progress
        green_value_text.setText(green_value.progress.toString())

        green_value.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b ->
                green_value_text.setText(i.toString())
                green = i
                brush_preview.setImageDrawable(col.apply { color = Color.argb(alpha, red, green, blue) })
            }
            onStopTrackingTouch { callback.adjustColor(alpha, red, green, blue) }
        }

        // Blue channel
        blue_value.progress = activity?.boxdrawing?.blue!!.toInt()
        blue = blue_value.progress
        blue_value_text.setText(blue_value.progress.toString())
        brush_preview.setImageDrawable(col.apply { color = Color.argb(alpha, red, green, blue) })

        blue_value.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b ->
                blue_value_text.setText(i.toString())
                blue = i
                brush_preview.setImageDrawable(col.apply { color = Color.argb(alpha, red, green, blue) })
            }
            onStopTrackingTouch { callback.adjustColor(alpha, red, green, blue) }
        }


    }
}