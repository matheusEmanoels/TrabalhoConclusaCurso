package br.edu.utfpr.trabalhoconclusaocurso.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.trabalhoconclusaocurso.R
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Atividade
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.AtividadeRepository
import br.edu.utfpr.trabalhoconclusaocurso.services.DBHelper
import br.edu.utfpr.trabalhoconclusaocurso.utils.DateValueFormatter
import br.edu.utfpr.trabalhoconclusaocurso.utils.PaceValueFormatter
import br.edu.utfpr.trabalhoconclusaocurso.utils.SessaoUsuario
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class RelFragment : Fragment() {
    private lateinit var lineChartDistancia: LineChart
    private lateinit var lineChartPace: LineChart
    private lateinit var barChartDuracao: BarChart
    private lateinit var dbHelper: DBHelper
    private lateinit var barChartDistanciaDiaria: BarChart
    private lateinit var atividadeRepository: AtividadeRepository
    private var usuario: Usuario? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rel, container, false)

        lineChartDistancia = view.findViewById(R.id.lineChartDistancia)
        lineChartPace = view.findViewById(R.id.lineChartPace)
        barChartDuracao = view.findViewById(R.id.barChartDuracao)
        barChartDistanciaDiaria = view.findViewById(R.id.barChartDistanciaDiaria)

        dbHelper = DBHelper(requireContext())
        atividadeRepository = AtividadeRepository(dbHelper.writableDatabase)

        usuario = SessaoUsuario.getUsuario()

        lifecycleScope.launch(Dispatchers.IO) {
            val atividades = atividadeRepository.listarPorUsuarioLocal(usuario?.id!!)
            if (atividades.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    configurarGraficoDistancia(atividades)
                    configurarGraficoPace(atividades)
                    configurarGraficoDuracaoSemanal(atividades)
                    configurarGraficoDistanciaDiaria(atividades)
                }
            }
        }

        return view
    }

    private fun configurarGraficoDistancia(atividades: List<Atividade>) {
        val entries = atividades.mapIndexed { index, atividade ->
            Entry(index.toFloat(), (atividade.distancia / 1000f).toFloat()) // já em km
        }

        val dataSet = LineDataSet(entries, "Distância percorrida (km)").apply {
            color = Color.BLUE
            lineWidth = 2f
            setCircleColor(Color.RED)
            circleRadius = 4f
            valueTextColor = Color.BLACK
        }

        lineChartDistancia.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            axisRight.isEnabled = false
            xAxis.valueFormatter = DateValueFormatter(atividades)
            xAxis.granularity = 1f
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            animateX(1000)
            invalidate()
        }
    }

    private fun configurarGraficoPace(atividades: List<Atividade>) {
        val entries = atividades.mapIndexed { index, atividade ->
            val pace = if (atividade.velocidadeMedia > 0) {
                60f / atividade.velocidadeMedia.toFloat()
            } else 0f
            Entry(index.toFloat(), pace)
        }

        val dataSet = LineDataSet(entries, "Pace médio (min/km)").apply {
            color = Color.MAGENTA
            lineWidth = 2f
            setCircleColor(Color.BLACK)
            circleRadius = 4f
            valueTextColor = Color.BLACK
        }

        lineChartPace.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            axisRight.isEnabled = false
            xAxis.valueFormatter = DateValueFormatter(atividades)
            xAxis.granularity = 1f
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            axisLeft.valueFormatter = PaceValueFormatter()
            animateX(1000)
            invalidate()
        }
    }

    private fun configurarGraficoDuracaoSemanal(atividades: List<Atividade>) {
        val calendar = Calendar.getInstance()

        val duracaoSemanal = atividades.groupBy { atividade ->
            val timestamp = atividade.dataHora.toLong()
            val date = Date(timestamp)
            calendar.time = date
            calendar.get(Calendar.WEEK_OF_YEAR)
        }.mapValues { entry ->
            entry.value.sumOf { it.duracao } / 3600.0
        }

        val entries = duracaoSemanal.map { (semana, horas) ->
            BarEntry(semana.toFloat(), horas.toFloat())
        }

        val dataSet = BarDataSet(entries, "Duração semanal (h)").apply {
            color = Color.GREEN
            valueTextColor = Color.BLACK
        }

        barChartDuracao.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            axisRight.isEnabled = false
            xAxis.granularity = 1f
            animateY(1000)
            invalidate()
        }
    }

    private fun configurarGraficoDistanciaDiaria(atividades: List<Atividade>) {
        val calendar = Calendar.getInstance()

        // Agrupar atividades por DIA
        val distanciaDiaria = atividades.groupBy { atividade ->
            val timestamp = atividade.dataHora.toLong()
            val date = Date(timestamp)
            calendar.time = date
            "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH)+1}"
        }.mapValues { entry ->
            entry.value.sumOf { it.distancia }
        }

        // Criar as entradas (BarEntry) com índice incremental
        val labels = distanciaDiaria.keys.toList()
        val entries = distanciaDiaria.values.mapIndexed { index, distancia ->
            BarEntry(index.toFloat(), (distancia / 1000f).toFloat())
        }

        val dataSet = BarDataSet(entries, "Distância diária (km)").apply {
            color = Color.CYAN
            valueTextColor = Color.BLACK
        }

        barChartDistanciaDiaria.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            axisRight.isEnabled = false
            xAxis.granularity = 1f
            xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            animateY(1000)
            invalidate()
        }
    }

}