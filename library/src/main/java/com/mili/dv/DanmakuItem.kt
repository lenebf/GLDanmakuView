package com.mili.dv

import android.graphics.Bitmap
import android.graphics.Canvas
import com.mili.glcanvas.GLCanvas


/**
 * @author mili_bit 2019-10-17
 */
abstract class DanmakuItem(
    val topPosition: Int,
    var leftPosition: Int,
    private val speed: Int
) {

    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas
    private var isReleased = false

    internal fun prepare() {
        val width = this.getWidth()
        val height = this.getHeight()
        require(!(width <= 0 || height <= 0)) { "The width and height must > 0." }
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        this.onDraw(canvas)
    }

    abstract fun getWidth(): Int

    abstract fun getHeight(): Int

    abstract fun onDraw(canvas: Canvas)

    open fun step() {
        if (isReleased) {
            return
        }
        leftPosition -= speed
    }

    fun draw(canvas: GLCanvas) {
        if (isReleased) {
            return
        }
        canvas.drawBitmap(bitmap, leftPosition.toFloat(), topPosition.toFloat())
    }

    open fun release() {
        isReleased = true
        canvas.setBitmap(null)
        bitmap.isRecycled.no {
            bitmap.recycle()
        }
    }
}