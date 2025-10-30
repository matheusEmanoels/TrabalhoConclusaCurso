package br.edu.utfpr.trabalhoconclusaocurso.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import br.edu.utfpr.trabalhoconclusaocurso.R
import br.edu.utfpr.trabalhoconclusaocurso.activities.SettingsActivity
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Atividade
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Coordenada
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.AtividadeRepository
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.CoordenadaRepository
import br.edu.utfpr.trabalhoconclusaocurso.utils.SessaoUsuario
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private var totalDistance = 0.0
    private var startTime: Long = 0
    private var usuarioLocal: Usuario? = null
    private var isPaused = false
    private var frequenciaCoordenadas = 2000L
    private var distanciaAcumulada = 0.0
    private var proximoAviso = 500
    private lateinit var dbHelper: DBHelper
    private lateinit var atividadeRepository: AtividadeRepository
    private lateinit var coordenadaRepository: CoordenadaRepository
    private lateinit var atividadeId: String
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startTime = System.currentTimeMillis()

        dbHelper = DBHelper(this)
        atividadeRepository = AtividadeRepository(dbHelper.writableDatabase)
        coordenadaRepository = CoordenadaRepository(dbHelper.writableDatabase)

        val freq = SettingsActivity.Config.getFrequenciaAtualizacao(this)
        frequenciaCoordenadas = if (freq in 1..59) (freq * 1000L) else 2000L

        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())

        atividadeId = intent?.getStringExtra("atividadeId") ?: ""
        usuarioLocal = SessaoUsuario.getUsuario()
        val action = intent?.getStringExtra("action") ?: "START"

        when (action) {
            "START" -> isPaused = false
            "PAUSE" -> isPaused = true
        }

        // Se for START e ainda não houver registro da atividade
        if (action == "START" && atividadeId.isNotEmpty()) {
            val novaAtividade = Atividade(
                id = atividadeId,
                idUsuario = usuarioLocal?.id!!,
                nome = "Corrida",
                dataHora = startTime.toString(),
                duracao = 0,
                distancia = 0.0,
                velocidadeMedia = 0.0,
                caloriasPerdidas = 0.0
            )
            serviceScope.launch {
                atividadeRepository.salvar(novaAtividade)
            }
        }

        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            frequenciaCoordenadas
        ).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (isPaused) return // Não grava pontos se estiver pausado

            for (location in result.locations) {
                if (lastLocation != null) {
                    val distance = lastLocation!!.distanceTo(location).toDouble()
                    totalDistance += distance
                }
                lastLocation = location

                val duracaoSegundos = (System.currentTimeMillis() - startTime) / 1000.0
                val duracaoHoras = duracaoSegundos / 3600.0
                val velocidadeMedia = if (duracaoHoras > 0) (totalDistance / 1000.0) / duracaoHoras else 0.0
                val pace = if (totalDistance > 0) (duracaoSegundos / 60.0) / (totalDistance / 1000.0) else 0.0
                val paceMin = pace.toInt()
                val paceSec = ((pace - paceMin) * 60).toInt()
                val calorias = calcGastoCalorias(velocidadeMedia, usuarioLocal?.peso!!, duracaoHoras)

                val coordenada = Coordenada(
                    id = UUID.randomUUID().toString(),
                    idAtividade = atividadeId,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                serviceScope.launch {
                    coordenadaRepository.salvar(coordenada, usuarioLocal?.id!!)
                }

                if(SettingsActivity.Config.isFeedbackAudioLigado(this@LocationService)) {
                    atualizarProgresso(totalDistance, duracaoSegundos, velocidadeMedia)
                }

                sendLocationToActivity(location, totalDistance, velocidadeMedia, calorias, paceMin, paceSec, duracaoSegundos)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)

        val duracaoSegundos = (System.currentTimeMillis() - startTime) / 1000.0
        val duracaoHoras = duracaoSegundos / 3600.0
        val velocidadeMedia = if (duracaoHoras > 0) (totalDistance / 1000.0) / duracaoHoras else 0.0
        val pace = if (totalDistance > 0) (duracaoSegundos / 60.0) / (totalDistance / 1000.0) else 0.0
        val paceMin = pace.toInt()
        val paceSec = ((pace - paceMin) * 60).toInt()
        val calorias = calcGastoCalorias(velocidadeMedia, usuarioLocal?.peso!!, duracaoHoras)

        val atividadeFinal = Atividade(
            id = atividadeId,
            idUsuario = usuarioLocal?.id!!,
            nome = "Corrida",
            dataHora = startTime.toString(),
            duracao = duracaoSegundos.toLong(),
            distancia = totalDistance,
            velocidadeMedia = velocidadeMedia,
            caloriasPerdidas = calorias
        )
        serviceScope.launch { atividadeRepository.atualizar(atividadeFinal) }

        sendResToActivity(paceMin, paceSec, totalDistance, velocidadeMedia, calorias, duracaoSegundos)
        serviceJob.cancel()
    }

    private fun createNotification(): Notification {
        val channelId = "location_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Localização em segundo plano",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("App de Corrida")
            .setContentText("Rastreamento em andamento...")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun sendLocationToActivity(
        location: Location,
        totalDistance: Double,
        velocidadeMedia: Double,
        calorias: Double,
        paceMin: Int,
        paceSec: Int,
        duracao: Double
    ) {
        val intent = Intent("LOCATION_UPDATE")
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        intent.putExtra("distancia", totalDistance)
        intent.putExtra("velocidade", velocidadeMedia)
        intent.putExtra("calorias", calorias)
        intent.putExtra("paceMin", paceMin)
        intent.putExtra("paceSec", paceSec)
        intent.putExtra("duracao", duracao)
        sendBroadcast(intent)
    }

    private fun sendResToActivity(
        paceMin: Int,
        paceSec: Int,
        totalDistance: Double,
        velocidadeMedia: Double,
        calorias: Double,
        duracao: Double
    ) {
        val intent = Intent("FINAL_UPDATE")
        intent.putExtra("distancia", totalDistance)
        intent.putExtra("velocidade", velocidadeMedia)
        intent.putExtra("calorias", calorias)
        intent.putExtra("paceMin", paceMin)
        intent.putExtra("paceSec", paceSec)
        intent.putExtra("duracao", duracao)
        sendBroadcast(intent)
    }

    private fun calcGastoCalorias(velocidade: Double, peso: Double, duracao: Double): Double {
        val MET = calcMET(velocidade)
        return MET * peso * (duracao / 3600.0)
    }

    private fun calcMET(velocidade: Double): Double {
        return when {
            velocidade >= 16.0 -> 16.0
            velocidade >= 13.0 -> 12.8
            velocidade >= 11.3 -> 11.5
            velocidade >= 9.5 -> 9.8
            velocidade >= 8.0 -> 8.0
            else -> 5.0
        }
    }

    private fun falar(texto: String) {
        val intent = Intent(this, TTSService::class.java)
        intent.putExtra("texto_para_falar", texto)
        startService(intent)
    }

    fun atualizarProgresso(distanciaNova: Double, horas: Double, velocidadeMedia: Double) {
        distanciaAcumulada = distanciaNova

        val objetivoMetros = (usuarioLocal?.distanciaPreferida?.toFloat() ?: 1f) * 1000f

        if (distanciaAcumulada >= proximoAviso) {
            val km = distanciaAcumulada / 1000f

            val minutesPerKm = if (km > 0) (horas / 60.0) / km else 0.0
            val paceMin = minutesPerKm.toInt()
            val paceSec = ((minutesPerKm - paceMin) * 60).toInt()
            val paceStr =
                String.format("%d minutos e %02d segundos por quilômetro", paceMin, paceSec)

            val restanteMetros = (objetivoMetros - distanciaAcumulada).coerceAtLeast(0.0)
            val restanteStr = if (restanteMetros >= 1000) {
                String.format("%.2f quilômetros", restanteMetros / 1000f)
            } else {
                String.format("%.0f metros", restanteMetros)
            }

            val distanciaStr = if (distanciaAcumulada < 1000) {
                String.format("%.0f metros", distanciaAcumulada)
            } else {
                String.format("%.2f quilômetros", km)
            }

            if (restanteMetros == 0.0) {
                falar("Parabéns! Você atingiu seu objetivo de ${"%.2f".format(objetivoMetros / 1000f)} quilômetros!")
            } else {
                falar(
                    "Você completou $distanciaStr. " +
                            "Sua velocidade média é ${"%.1f".format(velocidadeMedia)} quilômetros por hora. " +
                            "Seu ritmo médio é de $paceStr. " +
                            "Faltam $restanteStr para o seu objetivo."
                )
            }

            proximoAviso += 500
        }
    }
}
