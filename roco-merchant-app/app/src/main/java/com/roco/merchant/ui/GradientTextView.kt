package com.roco.merchant.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatTextView

/** 彩色流动渐变字体（愿望单商品）——REPEAT 平铺 + 首尾同色，循环无缝 */
class GradientTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    // 首尾同色（红色），保证循环点颜色连续
    private val colors = intArrayOf(
        0xFFE53935.toInt(), 0xFF7C5CFF.toInt(), 0xFF1E88E5.toInt(),
        0xFF43A047.toInt(), 0xFFFB8C00.toInt(), 0xFFE53935.toInt()
    )
    private var shader: LinearGradient? = null
    private var animator: ValueAnimator? = null
    private var offset = 0f
    private var lastText: String? = null

    init { setLayerType(LAYER_TYPE_SOFTWARE, null) }

    fun startFlow() {
        val text = text?.toString() ?: ""
        // 文字没变且动画仍在运行 → 保持当前进度继续流动（避免刷新/重绑时颜色跳变或动画重置）
        if (animator?.isRunning == true && lastText == text) return
        lastText = text
        animator?.cancel()
        val textW = paint.measureText(text)
        val cycle = (textW + 40f).coerceAtLeast(120f)
        // REPEAT：动画平移一个周期后，图案与起点完全一致 → 无缝循环
        shader = LinearGradient(0f, 0f, cycle, 0f, colors, null, Shader.TileMode.REPEAT)
        // 匀速运动：LinearInterpolator 无加减速；时长随周期等比，所有彩虹字速度一致（约 36px/s）
        val speedPxPerSec = 36f
        val durationMs = (cycle / speedPxPerSec * 1000f).toLong().coerceAtLeast(1500L)
        animator = ValueAnimator.ofFloat(0f, cycle).apply {
            duration = durationMs
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { offset = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    fun stopFlow() {
        animator?.cancel()
        shader = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (shader != null) {
            val m = Matrix().apply { setTranslate(offset, 0f) }
            shader?.setLocalMatrix(m)
            paint.shader = shader
        } else {
            paint.shader = null
        }
        super.onDraw(canvas)
    }

    override fun onDetachedFromWindow() {
        stopFlow()
        super.onDetachedFromWindow()
    }
}
