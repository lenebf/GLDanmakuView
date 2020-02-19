package com.mili.dv

import android.util.SparseIntArray
import com.mili.dv.DanmakuView.Companion.FPS
import kotlin.math.ceil


/**
 * @author mili_bit 2019-11-06
 */
class StaggerSpeedGenerator : SpeedGenerator {

    override fun generateSpeed(width: Int, lineCount: Int, speedMap: SparseIntArray): Int {
        val slow = ceil(width * 1.0 / (6 * FPS)).toInt()
        val middle = ceil(width * 1.0 / (5 * FPS)).toInt()
        val fast = ceil(width * 1.0 / (4 * FPS)).toInt()
        val lineNumberOffset: Int
        if (lineCount <= 1) {
            lineNumberOffset = 0
            speedMap.put(0, middle)
        } else {
            lineNumberOffset = (Math.random() * lineCount).toInt()
            for (i in 0 until lineCount) {
                when ((i + lineNumberOffset) % 3) {
                    0 -> speedMap.put(i, slow)
                    1 -> speedMap.put(i, middle)
                    else -> speedMap.put(i, fast)
                }
            }
        }
        return lineNumberOffset
    }
}