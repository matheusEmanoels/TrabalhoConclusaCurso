package br.edu.utfpr.trabalhoconclusaocurso.activities

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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


class RelatoriosActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart
    private lateinit var dbHelper: DBHelper
    private lateinit var atividadeRepository: AtividadeRepository
    private var usuario: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorios)

        lineChart = findViewById(R.id.lineChartDistancia)
        barChart = findViewById(R.id.barChartDuracao)

        dbHelper = DBHelper(this)
        atividadeRepository = AtividadeRepository(dbHelper.writableDatabase)

        usuario = SessaoUsuario.getUsuario()
        lifecycleScope.launch(Dispatchers.IO) {
            val atividades = atividadeRepository.listarPorUsuarioLocal(usuario?.id!!)
            if (atividades.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    configurarGraficoDistancia(atividades)
                    configurarGraficoDuracaoSemanal(atividades)
                    configurarGraficoPace(atividades)
                }
            }
        }
    }

    private fun configurarGraficoDistancia(atividades: List<Atividade>) {
        val entries = atividades.mapIndexed { index, atividade ->
            Entry(index.toFloat(), atividade.distancia.toFloat())
        }

        val dataSet = LineDataSet(entries, "Distância percorrida (km)").apply {
            color = Color.BLUE
            lineWidth = 2f
            setCircleColor(Color.RED)
            circleRadius = 4f
            valueTextColor = Color.BLACK
        }

        lineChart.apply {
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

        barChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            axisRight.isEnabled = false
            xAxis.granularity = 1f
            animateY(1000)
            invalidate()
        }
    }

    private fun configurarGraficoPace(atividades: List<Atividade>) {
        val entries = atividades.mapIndexed { index, atividade ->
            Entry(index.toFloat(), atividade.velocidadeMedia.toFloat())
        }

        val dataSet = LineDataSet(entries, "Pace médio (min/km)").apply {
            color = Color.MAGENTA
            lineWidth = 2f
            setCircleColor(Color.BLACK)
            circleRadius = 4f
            valueTextColor = Color.BLACK
        }

        lineChart.apply {
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
}