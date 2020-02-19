package com.mili.dv

import android.content.Context
import android.os.*
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.util.SparseIntArray
import androidx.core.util.isEmpty
import com.mili.glcanvas.GLCanvas
import com.mili.glcanvas.GLView
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.collections.ArrayList

/**
 * @author mili_bit 2019-07-11
 */
class DanmakuView : GLView {

    /** 弹幕水平间隔  */
    private var defaultHorizontalSpace: Int = 30.dp2px(context).toInt()
    private val drawing = AtomicBoolean(false)
    private val danmakuItems = SparseArray<MutableList<DanmakuItem>>()
    private var verticalSpace = 0
    private var maxLines = DEFAULT_MAX_LINES
    private var horizontalSpace = defaultHorizontalSpace
    private var lineCount = 0
    private var lineNumberOffset = 0
    private val topPositions = SparseIntArray()
    private val speeds = SparseIntArray()
    private val highLightNumber = AtomicInteger(0)
    private var itemBuilder: ItemBuilder? = null
    private var drawTimes: LinkedList<Long>? = null
    var speedGenerator: SpeedGenerator? = null
    var adapter: Adapter? = null
    /**弹幕绘制的FPS，Debug环境下表示真实FPS，Release环境下恒定为0*/
    var fps = 0

    constructor(context: Context) : super(context) {
        initAttrs(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.DanmakuView)
            if (ta != null) {
                maxLines = ta.getInt(R.styleable.DanmakuView_maxLines, DEFAULT_MAX_LINES)
                val resId = ta.getResourceId(R.styleable.DanmakuView_horizontalSpace, -1)
                horizontalSpace = if (resId > 0) {
                    context.resources.getDimensionPixelSize(resId)
                } else {
                    ta.getDimensionPixelSize(
                        R.styleable.DanmakuView_horizontalSpace,
                        defaultHorizontalSpace
                    )
                }
                ta.recycle()
            }
        }

        val ht = HandlerThread("danmaku_item_builder")
        ht.start()
        itemBuilder = ItemBuilder(ht.looper)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }

    override fun onGLDraw(canvas: GLCanvas) {
        if (topPositions.size() >= lineCount && verticalSpace > 0) {
            val toDrawItems = SparseArray<List<DanmakuItem>>()
            synchronized(danmakuItems) {
                for (i in 0 until danmakuItems.size()) {
                    val key = danmakuItems.keyAt(i)
                    val value = danmakuItems.valueAt(i)
                    toDrawItems.put(key, ArrayList(value))
                }
            }
            // 还没有插入弹幕，直接请求生成新的弹幕
            var needInsertDanmaku = toDrawItems.isEmpty()
            if (!needInsertDanmaku) {
                for (i in 0 until lineCount) {
                    val danmakuItemList = toDrawItems.get(i)
                    val key = toDrawItems.keyAt(i)
                    if (danmakuItemList == null || danmakuItemList.isEmpty()) {
                        needInsertDanmaku = true
                        continue
                    }
                    var itemVisible = LEFT_GONE
                    for (item in danmakuItemList) {
                        item.step()
                        itemVisible = getItemVisible(item)
                        if (itemVisible == VISIBLE_COMPLETELY || itemVisible == VISIBLE_PART) {
                            item.draw(canvas)
                        } else if (itemVisible == LEFT_GONE && itemBuilder != null) {
                            // 位于左边，并且完全不可见的需要移除
                            itemBuilder!!.obtainMessage(REMOVE_DANMAKU_ITEM, key, 0, item)
                                .sendToTarget()
                        }
                    }
                    // 最后一个弹幕已完全可见，需要添加新的弹幕
                    if (!needInsertDanmaku && itemVisible == VISIBLE_COMPLETELY) {
                        needInsertDanmaku = true
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                calculateFPS()
            }
            // 需要插入新的弹幕项
            if (needInsertDanmaku && itemBuilder != null) {
                itemBuilder!!.removeMessages(CHECK_DANMAKU_ITEMS)
                itemBuilder!!.obtainMessage(CHECK_DANMAKU_ITEMS).sendToTarget()
            }
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        if (speedGenerator == null) {
            speedGenerator = StaggerSpeedGenerator()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        if (width <= 0 || height <= 0 || maxLines <= 0) {
            return
        }
        val itemHeight = adapter!!.getItemHeight()
        val realMaxLines = height / itemHeight
        val newLineCount: Int = if (realMaxLines < maxLines) {
            1.coerceAtLeast(realMaxLines)
        } else {
            maxLines
        }
        if (newLineCount == lineCount) {
            return
        }
        lineCount = newLineCount
        highLightNumber.set(lineCount / 2)
        lineNumberOffset = speedGenerator!!.generateSpeed(width, lineCount, speeds)
        val newVerticalSpace = (height - lineCount * itemHeight) / (lineCount + 1)
        if (newVerticalSpace == verticalSpace) {
            return
        }
        verticalSpace = newVerticalSpace
        for (i in 0 until lineCount) {
            val topY = verticalSpace + i * (itemHeight + verticalSpace)
            topPositions.put(i, topY)
        }
    }

    override fun onPause() {
        drawing.set(false)
        super.onPause()
    }

    override fun onResume() {
        drawing.set(true)
        super.onResume()
    }

    /**
     *
     * @see DanmakuItemVisible
     */
    @DanmakuItemVisible
    fun getItemVisible(item: DanmakuItem?): Int {
        return if (item == null) {
            NONE
        } else {
            when {
                item.leftPosition + item.getWidth() <= 0 -> LEFT_GONE
                item.leftPosition >= right -> RIGHT_GONE
                item.leftPosition + item.getWidth() <= right -> VISIBLE_COMPLETELY
                else -> VISIBLE_PART
            }
        }
    }

    fun release() {
        if (itemBuilder != null) {
            itemBuilder!!.removeCallbacksAndMessages(null)
            itemBuilder!!.looper.quit()
        }
        synchronized(danmakuItems) {
            for (i in 0 until danmakuItems.size()) {
                val danmakuItemList = danmakuItems.valueAt(i)
                if (danmakuItemList != null && danmakuItemList.isNotEmpty()) {
                    for (danmakuItem in danmakuItemList) {
                        danmakuItem.release()
                    }
                }
            }
        }
    }

    private fun calculateFPS() {
        if (drawTimes == null) {
            drawTimes = LinkedList()
        }
        val lastTime = SystemClock.uptimeMillis()
        drawTimes!!.addLast(lastTime)
        val diffTime = (lastTime - drawTimes!!.first).toFloat()
        val frames = drawTimes!!.size
        if (frames > MAX_RECORD_SIZE) {
            drawTimes!!.removeFirst()
        }
        fps = (if (diffTime > 0) drawTimes!!.size * ONE_SECOND / diffTime else 0.0f).toInt()
    }

    private inner class ItemBuilder(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            adapter?.run {
                when (msg.what) {
                    CHECK_DANMAKU_ITEMS -> {
                        checkDanmakuItems()
                    }
                    REMOVE_DANMAKU_ITEM -> {
                        val key = msg.arg1
                        val item = msg.obj as DanmakuItem
                        removeDanmakuItem(key, item)
                    }
                }
            }
        }

        private fun checkDanmakuItems() {
            if (!drawing.get()) {
                return
            }
            if (danmakuItems.isEmpty()) {
                addDanmakuItemsFirst()
                return
            }
            for (i in 0 until lineCount) {
                val danmakuItemList = danmakuItems.get(i)
                var item: DanmakuItem? = null
                if (danmakuItemList != null && danmakuItemList.isNotEmpty()) {
                    item = danmakuItemList[danmakuItemList.size - 1]
                }
                // 该行没有弹幕或者最后一个弹幕已完全可见，需要添加新的弹幕
                val visibility = getItemVisible(item)
                if (visibility == NONE || visibility == VISIBLE_COMPLETELY) {
                    Log.d(
                        "debug_info",
                        "$item {\"leftPosition\":${item?.leftPosition}, \"visibility\":$visibility}"
                    )
                    val result = addDanmakuItem(i)
                    (visibility == VISIBLE_COMPLETELY).yes {
                        adapter!!.onDanmakuItemCompletelyVisible(i, item!!)
                    }
                    if (!result) {
                        break
                    }
                }
            }
        }

        private fun removeDanmakuItem(key: Int, item: DanmakuItem?) {
            if (item != null) {
                item.release()
                adapter!!.onDanmakuItemCompletelyGone(key, item)
                synchronized(danmakuItems) {
                    val danmakuItemList = danmakuItems.get(key)
                    danmakuItemList?.remove(item)
                }
            }
        }

        private fun addDanmakuItem(lineNumber: Int): Boolean {
            newDanmakuItem(lineNumber)?.run {
                synchronized(danmakuItems) {
                    var danmakuItemList: MutableList<DanmakuItem>? = danmakuItems.get(lineNumber)
                    if (danmakuItemList == null) {
                        danmakuItemList = mutableListOf()
                        danmakuItems.put(lineNumber, danmakuItemList)
                    }
                    danmakuItemList.add(this)
                }
                return true
            }
            return false
        }

        private fun addDanmakuItemsFirst() {
            if (!drawing.get() || !adapter!!.hasData()) {
                return
            }
            for (i in 0 until lineCount) {
                val lineNumber = (i + lineNumberOffset) % lineCount
                newDanmakuItem(lineNumber)?.run {
                    synchronized(danmakuItems) {
                        var danmakuItemList: MutableList<DanmakuItem>? =
                            danmakuItems.get(lineNumber)
                        if (danmakuItemList == null) {
                            danmakuItemList = mutableListOf()
                            danmakuItems.put(lineNumber, danmakuItemList)
                        }
                        danmakuItemList.add(this)
                    }
                }
            }
        }

        private fun newDanmakuItem(lineNumber: Int): DanmakuItem? {
            return adapter!!.onCreateDanmakuItem(
                lineNumber,
                topPositions.get(lineNumber),
                right + horizontalSpace,
                speeds.get(lineNumber)
            )?.apply {
                prepare()
            }
        }
    }

    abstract class Adapter {

        /**@return 返回最大的弹幕高度*/
        abstract fun getItemHeight(): Int

        /**
         * 创建新的弹幕项
         * @param lineNumber 行号，从0开始*/
        abstract fun onCreateDanmakuItem(
            lineNumber: Int,
            topPosition: Int,
            leftPosition: Int,
            speed: Int
        ): DanmakuItem?

        /**
         * 弹幕完全可见通知
         * @param lineNumber 行号，从0开始
         * @param item 完全可见的弹幕项*/
        fun onDanmakuItemCompletelyVisible(lineNumber: Int, item: DanmakuItem) {

        }

        /**
         * 弹幕完全消失通知
         * @param lineNumber 行号，从0开始
         * @param item 完全消失的弹幕项，注意item已被Release*/
        fun onDanmakuItemCompletelyGone(lineNumber: Int, item: DanmakuItem) {

        }

        /**适配器是否有有效数据，如果先生成了适配器，后设置数据需要注意这里的逻辑*/
        abstract fun hasData(): Boolean
    }

    companion object {

        /** 弹幕默认行数  */
        private const val DEFAULT_MAX_LINES = 3
        /** 帧率60  */
        const val FPS = 60

        private const val MAX_RECORD_SIZE = 80
        private const val ONE_SECOND = 1000

        private const val CHECK_DANMAKU_ITEMS = 1
        private const val REMOVE_DANMAKU_ITEM = 2
    }
}
