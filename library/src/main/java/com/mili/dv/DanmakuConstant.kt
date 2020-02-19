package com.mili.dv

import androidx.annotation.IntDef

/**
 * @author mili_bit 2019-11-07
 */

const val NONE = -3
const val LEFT_GONE = -2
const val RIGHT_GONE = -1
const val VISIBLE_PART = 0
const val VISIBLE_COMPLETELY = 1

@IntDef(NONE, LEFT_GONE, RIGHT_GONE, VISIBLE_PART, VISIBLE_COMPLETELY)
@Retention(AnnotationRetention.SOURCE)
annotation class DanmakuItemVisible