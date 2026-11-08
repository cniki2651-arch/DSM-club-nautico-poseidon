package com.dsm.clubnauticoposeidon.ui.components

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.ColorUtils
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class BarrasRedondeadasRenderer(
    chart: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
    private val radio: Float
) : BarChartRenderer(chart, animator, viewPortHandler) {

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans = mChart.getTransformer(dataSet.axisDependency)
        val buffer = mBarBuffers[index]
        buffer.setPhases(mAnimator.phaseX, mAnimator.phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        buffer.setBarWidth(mChart.barData.barWidth)
        buffer.feed(dataSet)
        trans.pointValuesToPixel(buffer.buffer)

        var j = 0
        while (j < buffer.size()) {
            val izq = buffer.buffer[j]
            val arriba = buffer.buffer[j + 1]
            val der = buffer.buffer[j + 2]
            val abajo = buffer.buffer[j + 3]

            if (!mViewPortHandler.isInBoundsLeft(der)) { j += 4; continue }
            if (!mViewPortHandler.isInBoundsRight(izq)) break

            // Degradado: color pleno arriba, casi transparente abajo
            val color = dataSet.getColor(j / 4)
            mRenderPaint.shader = LinearGradient(
                izq, arriba, izq, abajo,
                color, ColorUtils.setAlphaComponent(color, 70),
                Shader.TileMode.CLAMP
            )
            c.drawRoundRect(RectF(izq, arriba, der, abajo), radio, radio, mRenderPaint)
            j += 4
        }
        mRenderPaint.shader = null
    }
}