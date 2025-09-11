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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService : Service(){

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private var totalDistance = 0.0
    private var startTime: Long = 0

    private val userWeightKg = 70.0 // peso do usuário (exemplo)

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startTime = System.currentTimeMillis()
        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
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
                val calorias = (totalDistance / 1000.0) * userWeightKg

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
        val calorias = (totalDistance / 1000.0) * userWeightKg

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