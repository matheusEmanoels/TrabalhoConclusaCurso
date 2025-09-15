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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.edu.utfpr.trabalhoconclusaocurso.R
import br.edu.utfpr.trabalhoconclusaocurso.data.dao.CoordenadaDao
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Atividade
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Coordenada
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.AtividadeRepository
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.CoordenadaRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class LocationService : Service(){

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private var totalDistance = 0.0
    private var startTime: Long = 0
    private var usuarioLocal: Usuario? = null
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

        atividadeId = UUID.randomUUID().toString()


        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        usuarioLocal = intent?.getSerializableExtra("usuario") as? Usuario
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
        serviceScope.launch{
            atividadeRepository.salvar(novaAtividade)
        }

        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        ).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                if (lastLocation != null) {
                    val distance = lastLocation!!.distanceTo(location).toDouble()
                    totalDistance += distance
                }
                lastLocation = location

                val duracao = System.currentTimeMillis() - startTime
                val horas = duracao / 1000.0 / 3600.0

                val velocidadeMedia = if (horas > 0) (totalDistance / 1000.0) / horas else 0.0

                val calorias = calcGastoCalorias(velocidadeMedia, usuarioLocal?.peso!!, horas)

                val coordenada = Coordenada(
                    id = UUID.randomUUID().toString(),
                    idAtividade = atividadeId,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                serviceScope.launch {
                    coordenadaRepository.salvar(coordenada, usuarioLocal?.id!!)
                }

                if (totalDistance >= 1000) {
                    val km = (totalDistance / 1000).toInt()

                    val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0 // segundos
                    val hours = elapsedTime / 3600.0
                    val avgSpeed = if (hours > 0) totalDistance / 1000.0 / hours else 0.0 // km/h

                    val minutesPerKm = if (km > 0) (elapsedTime / 60.0) / km else 0.0
                    val paceMin = minutesPerKm.toInt()
                    val paceSec = ((minutesPerKm - paceMin) * 60).toInt()

                    val paceStr = String.format("%d minutos e %02d segundos por quilômetro", paceMin, paceSec)

                    falar("Você completou $km quilômetro${if (km > 1) "s" else ""}. " +
                            "Sua velocidade média é ${"%.1f".format(avgSpeed)} quilômetros por hora. " +
                            "Seu ritmo médio é de $paceStr.")

                    totalDistance %= 1000
                }
                sendLocationToActivity(location, totalDistance, velocidadeMedia, calorias)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)

        val duracao = System.currentTimeMillis() - startTime
        val horas = duracao / 1000.0 / 3600.0
        val velocidadeMedia = if (horas > 0) (totalDistance / 1000.0) / horas else 0.0
        val calorias = calcGastoCalorias(velocidadeMedia, usuarioLocal?.peso!!, horas)

        val atividadeFinal = Atividade(
            id = atividadeId,
            idUsuario = usuarioLocal?.id!!,
            nome = "Corrida",
            dataHora = startTime.toString(),
            duracao = duracao,
            distancia = totalDistance,
            velocidadeMedia = velocidadeMedia,
            caloriasPerdidas = calorias
        )
        serviceScope.launch {
            atividadeRepository.atualizar(atividadeFinal)
        }


        Log.d("GPS_SERVICE", "== RESUMO DA ATIVIDADE ==")
        Log.d("GPS_SERVICE", "Duração: ${duracao / 1000} s")
        Log.d("GPS_SERVICE", "Distância: ${"%.2f".format(totalDistance / 1000)} km")
        Log.d("GPS_SERVICE", "Velocidade Média: ${"%.2f".format(velocidadeMedia)} km/h")
        Log.d("GPS_SERVICE", "Calorias: ${"%.2f".format(calorias)} kcal")

        serviceJob.cancel()
    }

    private fun createNotification(): Notification {
        val channelId = "location_channel"
        val channelName = "Localização em segundo plano"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("App de Corrida")
            .setContentText("Rastreamento em andamento...")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun sendLocationToActivity(location: Location, totalDistance: Double, velocidadeMedia: Double, calorias: Double) {
        val intent = Intent("LOCATION_UPDATE")
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        intent.putExtra("distancia", totalDistance)
        intent.putExtra("velocidade", velocidadeMedia)
        intent.putExtra("calorias", calorias)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun falar(texto: String) {
        val intent = Intent(this, TTSService::class.java)
        intent.putExtra("texto_para_falar", texto)
        startService(intent)
    }

    private fun calcGastoCalorias(velocidade: Double, peso: Double, duracao: Double): Double {
        val MET = calcMET(velocidade)

        val caloriasTotais = MET * peso * duracao;

        return caloriasTotais;
    }

    private fun calcMET(velocidade: Double): Double {
        val MET = when {
            velocidade >= 16.0 -> 16.0
            velocidade >= 13.0 -> 12.8
            velocidade >= 11.3 -> 11.5
            velocidade >= 9.5 -> 9.8
            velocidade >= 8.0 -> 8.0
            else -> 5.0
        }
        return MET
    }
}