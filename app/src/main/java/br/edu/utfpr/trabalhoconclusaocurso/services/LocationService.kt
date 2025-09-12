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
import br.edu.utfpr.trabalhoconclusaocurso.R
import br.edu.utfpr.trabalhoconclusaocurso.data.dao.AtividadeDao
import br.edu.utfpr.trabalhoconclusaocurso.data.dao.CoordenadaDao
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Atividade
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Coordenada
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.UUID

class LocationService : Service(){

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private var totalDistance = 0.0
    private var startTime: Long = 0
    private var usuarioLocal: Usuario? = null
    private lateinit var dbHelper: DBHelper
    private lateinit var atividadeDao: AtividadeDao
    private lateinit var coordenadaDao: CoordenadaDao
    private lateinit var atividadeId: String

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startTime = System.currentTimeMillis()

        dbHelper = DBHelper(this)
        atividadeDao = AtividadeDao(dbHelper.writableDatabase)
        coordenadaDao = CoordenadaDao(dbHelper.writableDatabase)


        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        usuarioLocal = intent?.getSerializableExtra("usuario") as? Usuario
        atividadeId = UUID.randomUUID().toString()
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
        atividadeDao.inserir(novaAtividade)
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

                // duração
                val duracao = System.currentTimeMillis() - startTime
                val horas = duracao / 1000.0 / 3600.0

                // velocidade média (km/h)
                val velocidadeMedia = if (horas > 0) (totalDistance / 1000.0) / horas else 0.0

                // gasto calórico (fórmula simples)
                val calorias = (totalDistance / 1000.0) * usuarioLocal?.peso!!

                val coordenada = Coordenada(
                    idAtividade = atividadeId,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                coordenadaDao.inserir(coordenada)

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
        val calorias = (totalDistance / 1000.0) * usuarioLocal?.peso!!

        val atividadeFinal = Atividade(
            id = atividadeId,
            idUsuario = "USUARIO_TESTE",
            nome = "Corrida",
            dataHora = startTime.toString(),
            duracao = duracao,
            distancia = totalDistance,
            velocidadeMedia = velocidadeMedia,
            caloriasPerdidas = calorias
        )
        atividadeDao.atualizar(atividadeFinal)

        Log.d("GPS_SERVICE", "== RESUMO DA ATIVIDADE ==")
        Log.d("GPS_SERVICE", "Duração: ${duracao / 1000} s")
        Log.d("GPS_SERVICE", "Distância: ${"%.2f".format(totalDistance / 1000)} km")
        Log.d("GPS_SERVICE", "Velocidade Média: ${"%.2f".format(velocidadeMedia)} km/h")
        Log.d("GPS_SERVICE", "Calorias: ${"%.2f".format(calorias)} kcal")
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
        sendBroadcast(intent)
    }
}