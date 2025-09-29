package br.edu.utfpr.trabalhoconclusaocurso.utils

import br.edu.utfpr.trabalhoconclusaocurso.data.model.Atividade
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class DateValueFormatter(private val atividades: List<Atividade>) : ValueFormatter() {

    private val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        val index = value.toInt()
        if (index < 0 || index >= atividades.size) return ""

        val atividade = atividades[index]
        return try {
            val timestamp = atividade.dataHora.toLong()
            val date = Date(timestamp)
            outputFormat.format(date)
        } catch (e: NumberFormatException) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = sdf.parse(atividade.dataHora)
                outputFormat.format(date!!)
            } catch (ex: Exception) {
                ""
            }
        }
    }
}