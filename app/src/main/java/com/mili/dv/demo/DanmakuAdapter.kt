package com.mili.dv.demo

import android.content.Context
import com.mili.dv.DanmakuItem
import com.mili.dv.DanmakuView
import com.mili.dv.dp2px

/**
 * @author mili_bit 2019-11-06
 */
class DanmakuAdapter(private val context: Context) : DanmakuView.Adapter() {

    private val height = 60.dp2px(context).toInt()
    private val sampleData = arrayListOf(
        SimpleDanmakuBean("新语言自然有新特性", R.drawable.ic_gx),
        SimpleDanmakuBean("还保持 Java 的编程习惯去写 Kotlin", R.drawable.ic_whh),
        SimpleDanmakuBean("最近公众号「谷歌开发者", R.drawable.ic_gx),
        SimpleDanmakuBean("就举例了一些 Kotlin 编码的小技巧", R.drawable.ic_gx),
        SimpleDanmakuBean("既然是一种指南性质的文章", R.drawable.ic_gx),
        SimpleDanmakuBean("自然在「多而广」的基础上", R.drawable.ic_whh),
        SimpleDanmakuBean("有意去省略一些细节", R.drawable.ic_gx),
        SimpleDanmakuBean("同时举例的场景", R.drawable.ic_gx),
        SimpleDanmakuBean("可能还有一些不恰当的地方", R.drawable.ic_gx),
        SimpleDanmakuBean("这里我就来补齐这些细节", R.drawable.ic_whh),
        SimpleDanmakuBean("今天聊聊利用 Kotlin 的方法默认参数的特性", R.drawable.ic_gx),
        SimpleDanmakuBean("完成类似 Java 的方法重载的效果", R.drawable.ic_gx),
        SimpleDanmakuBean("完全解析这个特性的使用方式和原理", R.drawable.ic_whh),
        SimpleDanmakuBean("以及在使用过程中的一个深坑", R.drawable.ic_gx),
        SimpleDanmakuBean("我们可以在同一个类中", R.drawable.ic_gx),
        SimpleDanmakuBean("在这个例子中", R.drawable.ic_whh),
        SimpleDanmakuBean("和前面 Java 实现的效果是一致的", R.drawable.ic_gx),
        SimpleDanmakuBean("特性申明的方法", R.drawable.ic_whh),
        SimpleDanmakuBean("会被编译成虚拟机可识别", R.drawable.ic_whh),
        SimpleDanmakuBean("使用指定默认参数的方式", R.drawable.ic_gx),
        SimpleDanmakuBean("新语言自然有新特性", R.drawable.ic_gx),
        SimpleDanmakuBean("还保持 Java 的编程习惯去写 Kotlin", R.drawable.ic_whh),
        SimpleDanmakuBean("最近公众号「谷歌开发者", R.drawable.ic_gx),
        SimpleDanmakuBean("就举例了一些 Kotlin 编码的小技巧", R.drawable.ic_gx),
        SimpleDanmakuBean("既然是一种指南性质的文章", R.drawable.ic_gx),
        SimpleDanmakuBean("自然在「多而广」的基础上", R.drawable.ic_whh),
        SimpleDanmakuBean("有意去省略一些细节", R.drawable.ic_gx),
        SimpleDanmakuBean("同时举例的场景", R.drawable.ic_gx),
        SimpleDanmakuBean("可能还有一些不恰当的地方", R.drawable.ic_gx),
        SimpleDanmakuBean("这里我就来补齐这些细节", R.drawable.ic_whh),
        SimpleDanmakuBean("今天聊聊利用 Kotlin 的方法默认参数的特性", R.drawable.ic_gx),
        SimpleDanmakuBean("完成类似 Java 的方法重载的效果", R.drawable.ic_gx),
        SimpleDanmakuBean("完全解析这个特性的使用方式和原理", R.drawable.ic_whh),
        SimpleDanmakuBean("以及在使用过程中的一个深坑", R.drawable.ic_gx),
        SimpleDanmakuBean("我们可以在同一个类中", R.drawable.ic_gx),
        SimpleDanmakuBean("在这个例子中", R.drawable.ic_whh),
        SimpleDanmakuBean("和前面 Java 实现的效果是一致的", R.drawable.ic_gx),
        SimpleDanmakuBean("特性申明的方法", R.drawable.ic_whh),
        SimpleDanmakuBean("会被编译成虚拟机可识别", R.drawable.ic_whh),
        SimpleDanmakuBean("使用指定默认参数的方式", R.drawable.ic_gx)
    )
    private var showIndex = 0

    override fun getItemHeight(): Int {
        return height
    }

    override fun onCreateDanmakuItem(
        lineNumber: Int,
        topPosition: Int,
        leftPosition: Int,
        speed: Int
    ): DanmakuItem? {
        val realIndex = showIndex % sampleData.size
        return SimpleDanmakuItem(
            context,
            sampleData[realIndex],
            topPosition,
            leftPosition,
            speed
        ).also {
            showIndex++
        }
    }

    override fun hasData(): Boolean {
        return sampleData.size > 0
    }
}