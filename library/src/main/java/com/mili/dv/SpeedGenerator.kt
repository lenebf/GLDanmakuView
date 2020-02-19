package com.mili.dv

import android.util.SparseIntArray

/**
 * 速度生成器，给每一行弹幕生成一个移动速度
 * @author mili_bit 2019-11-06
 */
interface SpeedGenerator {

    /**
     * @param width 弹幕视图宽度
     * @param lineCount 弹幕行数
     * @param speedMap 存放弹幕速度的SparseIntArray,lineCount -> speed
     * @return 返回弹幕起始行偏移量，返回 0 until lineCount*/
    fun generateSpeed(width: Int, lineCount: Int, speedMap: SparseIntArray): Int
}