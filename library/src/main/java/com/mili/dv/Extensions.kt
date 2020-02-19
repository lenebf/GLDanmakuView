package com.mili.dv

import android.content.Context
import android.util.TypedValue

/**
 * @author mili_bit 2019-10-17
 */

fun Number.dp2px(context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
    )
}

inline fun Boolean?.yes(block: () -> Any): Boolean? {
    if (this != null && this == true) {
        block.invoke()
    }
    return this
}

inline fun Boolean?.no(block: () -> Any): Boolean? {
    if (this == null || this == false) {
        block.invoke()
    }
    return this
}

inline fun runSafe(block: () -> Any?): Any? {
    try {
        return block.invoke()
    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }
    return null
}