package com.dsm.clubnauticoposeidon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.lerp

data class DatoGrafico(val etiqueta: String, val valor: Int, val color: Color)

private val FormatoEntero = object : ValueFormatter() {
    override fun getFormattedValue(value: Float) = value.toInt().toString()
}

/** Tarjeta contenedora con el mismo estilo de tus tarjetas del panel */
@Composable
fun TarjetaGrafico(
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val forma = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Navy800)
            .border(1.dp, Color.White.copy(alpha = 0.08f), forma)
            .padding(20.dp)
    ) {
        Text(titulo, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(subtitulo, color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        contenido()
    }
}

@Composable
fun BarrasPedidos(datos: List<DatoGrafico>, modifier: Modifier = Modifier) {
    val colorValor = Ink.toArgb()
    val colorEtiqueta = Muted.toArgb()
    val colorGrilla = Color.White.copy(alpha = 0.08f).toArgb()

    AndroidView(
        modifier = modifier.fillMaxWidth().height(200.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                isHighlightPerTapEnabled = false
                setDrawGridBackground(false)
                setFitBars(true)
                setExtraTopOffset(16f)
                axisRight.isEnabled = false

                axisLeft.apply {
                    axisMinimum = 0f
                    granularity = 1f
                    setDrawAxisLine(false)
                    setDrawLabels(false)
                    gridColor = colorGrilla
                    enableGridDashedLine(12f, 10f, 0f)
                }
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    textColor = colorEtiqueta
                    textSize = 11f
                }

                this.renderer = BarrasRedondeadasRenderer(
                    this, animator, viewPortHandler,
                    radio = 10f * context.resources.displayMetrics.density
                )
                animateY(700, Easing.EaseOutCubic)
            }
        },
        update = { chart ->
            val entradas = datos.mapIndexed { i, d -> BarEntry(i.toFloat(), d.valor.toFloat()) }
            val set = BarDataSet(entradas, "Pedidos").apply {
                setColors(datos.map { it.color.toArgb() })
                valueTextColor = colorValor
                valueTextSize = 13f
                valueFormatter = FormatoEntero
            }
            // Margen superior para que el número encima de la barra no se corte
            val maximo = (datos.maxOfOrNull { it.valor } ?: 0).coerceAtLeast(1)
            chart.axisLeft.axisMaximum = maximo * 1.3f

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(datos.map { it.etiqueta })
            chart.xAxis.labelCount = datos.size
            chart.data = BarData(set).apply { barWidth = 0.45f }
            chart.invalidate()
        }
    )
}

@Composable
fun DonaPedidos(datos: List<DatoGrafico>, modifier: Modifier = Modifier) {
    val total = datos.sumOf { it.valor }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dona con el total superpuesto en el centro
        Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = false
                        isRotationEnabled = false
                        isHighlightPerTapEnabled = false
                        setDrawEntryLabels(false)
                        setDrawCenterText(false)
                        isDrawHoleEnabled = true
                        holeRadius = 72f
                        setHoleColor(AndroidColor.TRANSPARENT)
                        setTransparentCircleAlpha(0)
                        setExtraOffsets(4f, 4f, 4f, 4f)
                        animateY(800, Easing.EaseInOutQuad)
                    }
                },
                update = { chart ->
                    val visibles = datos.filter { it.valor > 0 }
                    val entradas = if (total == 0) listOf(PieEntry(1f, ""))
                    else visibles.map { PieEntry(it.valor.toFloat(), it.etiqueta) }
                    val colores = if (total == 0) listOf(Color.White.copy(alpha = 0.10f).toArgb())
                    else visibles.map { it.color.toArgb() }

                    val set = PieDataSet(entradas, "").apply {
                        setColors(colores)
                        sliceSpace = 3f
                        setDrawValues(false)
                    }
                    chart.data = PieData(set)
                    chart.invalidate()
                }
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$total", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("pedidos", color = Muted, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.width(20.dp))

        // Leyenda hecha en Compose
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            datos.forEach { d ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(d.color))
                    Spacer(Modifier.width(8.dp))
                    Text(d.etiqueta, color = Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(
                        "${d.valor}", color = Ink,
                        fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}