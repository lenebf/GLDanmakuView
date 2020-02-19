package com.mili.dv.demo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.mili.dv.DanmakuItem
import com.mili.dv.dp2px
import kotlin.math.abs

/**
 * @author mili_bit 2019-11-07
 */
class SimpleDanmakuItem(
    private val context: Context,
    private val data: SimpleDanmakuBean,
    topPosition: Int,
    leftPosition: Int,
    speed: Int
) : DanmakuItem(topPosition, leftPosition, speed) {

    private val textHeight = 30.dp2px(context).toInt()
    private val height = 50.dp2px(context).toInt()
    private val padding = 15.dp2px(context).toInt()
    private var width = 0
    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        isSubpixelText = true
        color = Color.WHITE
        textSize = 12.dp2px(context)
    }
    private val textBaseLine = height / 2 + abs(paint.ascent() + paint.descent()) / 2

    private val bgDrawable = GradientDrawable(
        GradientDrawable.Orientation.RIGHT_LEFT,
        intArrayOf(Color.parseColor("#4d002354"), Color.parseColor("#ff00AADD"))
    ).apply {
        shape = GradientDrawable.RECTANGLE
        setStroke(2, Color.parseColor("#ffffffff"))
        cornerRadius = (textHeight / 2).toFloat()
    }

    private var iconDrawable: Drawable? = null

    init {
        if (data.icon > 0) {
            iconDrawable = ContextCompat.getDrawable(context, data.icon)
        }
    }

    override fun getWidth(): Int {
        if (width <= 0) {
            val textWidth = paint.measureText(data.text).toInt()
            width = padding + textWidth + padding + if (data.icon > 0) {
                height
            } else {
                0
            }
        }
        return width
    }

    override fun getHeight(): Int {
        return height
    }

    override fun onDraw(canvas: Canvas) {
        val top =  (height - textHeight) / 2
        val right = if (iconDrawable == null) {
             width
        } else {
            width - height / 2
        }
        val bottom = top + textHeight
        bgDrawable.setBounds(0, top, right, bottom)
        bgDrawable.draw(canvas)
        canvas.drawText(data.text, padding.toFloat(), textBaseLine, paint)
        iconDrawable?.run {
            val left = width - height
            setBounds(left, 0, width, height)
            draw(canvas)
        }
    }
}