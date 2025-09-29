package br.edu.utfpr.trabalhoconclusaocurso.utils

import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.math.floor
import kotlin.math.roundToInt

class PaceValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val minutos = floor(value).toInt()
        val segundos = ((value - minutos) * 60).roundToInt()
        return String.format("%d'%02d\"", minutos, segundos)
    }
}