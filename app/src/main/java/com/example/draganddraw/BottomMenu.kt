package com.example.draganddraw

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_menu.*
import org.jetbrains.anko.sdk27.coroutines.onSeekBarChangeListener

class BottomMenu : BottomSheetDialogFragment() {
    interface BottomMenuCallback {
        fun adjustBrushSize(value: Int)
        fun adjustEraser(value: Int)
        fun adjustColor(alpha: Int, red: Int, green: Int, blue: Int)
    }

    private lateinit var callback: BottomMenuCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = activity as BottomMenuCallback
    }

    // read currentPosition settings
    private var tempAlpha = -1
    private var tempRed = -1
    private var tempGreen = -1
    private var tempBlue = -1
    private val tempColor = Color.argb(tempAlpha, tempRed, tempGreen, tempBlue).toDrawable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Don't display keyboard automatically
        // dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        // Brush size
        brush_size.progress = activity?.boxdrawing?.brushSize!!.toInt()
        brush_size_text.text = brush_size.progress.toString()

        brush_size.onSeekBarChangeListener {
            onProgressChanged { _, i, _ -> brush_size_text.text = i.toString() }
            onStopTrackingTouch { callback.adjustBrushSize(it?.progress!!) }
        }

        // Eraser size
        eraser_size.progress = activity?.boxdrawing?.eraserSize!!.toInt()
        eraser_size_text.text = eraser_size.progress.toString()

        eraser_size.onSeekBarChangeListener {
            onProgressChanged { _, i, _ -> eraser_size_text.text = i.toString() }
            onStopTrackingTouch { callback.adjustEraser(it?.progress!!) }
        }

        // Alpha channel
        alpha_value.progress = activity?.boxdrawing?.alpha!!.toInt()
        tempAlpha = alpha_value.progress
        alpha_value_text.text = alpha_value.progress.toString()

        alpha_value.onSeekBarChangeListener {
            onProgressChanged { _, i, _ ->
                alpha_value_text.text = i.toString()
                tempAlpha = i
//                brush_preview.setImageDrawable(tempColor.apply { tempAlpha = this.tempAlpha })
                brush_preview.setImageDrawable(tempColor.apply { color = Color.argb(i, tempRed, tempGreen, tempBlue) })
            }
            onStopTrackingTouch { callback.adjustColor(tempAlpha, tempRed, tempGreen, tempBlue) }
        }

        // Red channel
        red_value.progress = activity?.boxdrawing?.red!!.toInt()
        tempRed = red_value.progress
        red_value_text.text = red_value.progress.toString()

        red_value.onSeekBarChangeListener {
            onProgressChanged { _, i, _ ->
                red_value_text.text = i.toString()
                tempRed = i
                brush_preview.setImageDrawable(tempColor.apply { color = Color.argb(alpha, tempRed, tempGreen, tempBlue) })
            }
            onStopTrackingTouch { callback.adjustColor(tempAlpha, tempRed, tempGreen, tempBlue) }
        }

        // Green channel
        green_value.progress = activity?.boxdrawing?.green!!.toInt()
        tempGreen = green_value.progress
        green_value_text.text = green_value.progress.toString()

        green_value.onSeekBarChangeListener {
            onProgressChanged { _, i, _ ->
                green_value_text.text = i.toString()
                tempGreen = i
                brush_preview.setImageDrawable(tempColor.apply { color = Color.argb(alpha, tempRed, tempGreen, tempBlue) })
            }
            onStopTrackingTouch { callback.adjustColor(tempAlpha, tempRed, tempGreen, tempBlue) }
        }

        // Blue channel
        blue_value.progress = activity?.boxdrawing?.blue!!.toInt()
        tempBlue = blue_value.progress
        blue_value_text.text = blue_value.progress.toString()
        brush_preview.setImageDrawable(tempColor.apply { color = Color.argb(alpha, tempRed, tempGreen, tempBlue) })

        blue_value.onSeekBarChangeListener {
            onProgressChanged { _, i, _ ->
                blue_value_text.text = i.toString()
                tempBlue = i
                brush_preview.setImageDrawable(tempColor.apply { color = Color.argb(alpha, tempRed, tempGreen, tempBlue) })
            }
            onStopTrackingTouch { callback.adjustColor(tempAlpha, tempRed, tempGreen, tempBlue) }
        }
    }
}