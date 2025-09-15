package br.edu.utfpr.trabalhoconclusaocurso.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.edu.utfpr.trabalhoconclusaocurso.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import br.edu.utfpr.trabalhoconclusaocurso.services.LocationService
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnIniciarParar: Button
    private lateinit var btnOk: Button
    private lateinit var cvResumo : CardView
    private lateinit var tvDistancia: TextView
    private lateinit var tvVelocidade: TextView
    private lateinit var tvPace: TextView
    private lateinit var tvCalorias: TextView
    private lateinit var tvDuracao: TextView
    private var usuario: Usuario? = null
    private var isTracking = false
    private var polylineOptions = PolylineOptions().width(10f).color(android.graphics.Color.BLUE)
    private var primeiraPosicao = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        btnIniciarParar = findViewById(R.id.btn_start_stop)
        btnIniciarParar.setBackgroundColor(getColor(R.color.success))
        btnOk = findViewById(R.id.btn_ok)
        btnOk.setBackgroundColor(getColor(R.color.success))
        cvResumo = findViewById(R.id.info_panel)
        tvDistancia = findViewById(R.id.tv_distancia)
        tvVelocidade = findViewById(R.id.tv_velocidade)
        tvPace = findViewById(R.id.tv_pace)
        tvCalorias = findViewById(R.id.tv_calorias)
        tvDuracao = findViewById(R.id.tv_duracao)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        usuario = intent?.getSerializableExtra("usuario") as? Usuario
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.moveCamera(CameraUpdateFactory.zoomTo(22f))
        map.setOnMyLocationChangeListener { location ->
            if (primeiraPosicao && location != null) {
                val latLng = LatLng(location.latitude, location.longitude)

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                primeiraPosicao = false
            }
        }
    }

    fun OnClickIniciarParar(view: View) {
        if(isTracking){
            btnIniciarParar.text = "Iniciar"
            btnIniciarParar.setBackgroundColor(getColor(R.color.success))
            val serviceIntent = Intent(this, LocationService::class.java)
            stopService(serviceIntent)
        }else{
            btnIniciarParar.text = "Parar"
            btnIniciarParar.setBackgroundColor(getColor(R.color.error))
            val serviceIntent = Intent(this, LocationService::class.java)
            serviceIntent.putExtra("usuario", usuario)
            startService(serviceIntent)
        }
        isTracking = !isTracking
    }

    fun OnClickConfiguracoes(view: View) {}

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("LOCATION_UPDATE")
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver, filter)
        LocalBroadcastManager.getInstance(this).registerReceiver(finalReceiver, IntentFilter("FINAL_UPDATE"))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(finalReceiver)
    }

    fun OnClickOk(view: View) {
        cvResumo.visibility = View.GONE
        map.clear()
        polylineOptions = PolylineOptions()
        tvDistancia.text = "Distância: 0.00 km"
        tvVelocidade.text = "Velocidade Média: 0.0 km/h"
        tvPace.text = "Pace Médio: 0'00\" /km"
        tvCalorias.text = "Calorias: 0 kcal"
        tvDuracao.text = "Duração: 00:00:00"
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "LOCATION_UPDATE") {
                val lat = intent.getDoubleExtra("latitude", 0.0)
                val lon = intent.getDoubleExtra("longitude", 0.0)


                val ponto = LatLng(lat, lon)

                polylineOptions.add(ponto)
                map.clear()
                map.addPolyline(polylineOptions)
                map.addMarker(MarkerOptions().position(ponto).title("Posição atual"))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(ponto, 17f))
            }
        }
    }

    private val finalReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "FINAL_UPDATE"){
                tvDistancia.text = "Distância: %.2f km".format(intent.getDoubleExtra("distancia", 0.0))
                tvVelocidade.text = "Velocidade Média: %.2f km/h".format(intent.getDoubleExtra("velocidade", 0.0))
                tvPace.text = "Pace Médio: %d'%02d\" /km".format(intent.getIntExtra("paceMin", 0), intent.getIntExtra("paceSec", 0))
                tvCalorias.text = "Calorias: %.0f kcal".format(intent.getDoubleExtra("calorias", 0.0))

                val horas = intent.getIntExtra("duracao", 0) / 3600
                val minutos = (intent.getIntExtra("duracao", 0) % 3600) / 60
                val segundos = intent.getIntExtra("duracao", 0) % 60
                tvDuracao.text = "Duração: %02d:%02d:%02d".format(horas, minutos, segundos)

                cvResumo.visibility = View.VISIBLE
            }
        }
    }
}