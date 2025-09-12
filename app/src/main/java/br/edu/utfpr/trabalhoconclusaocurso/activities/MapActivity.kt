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
import android.util.Log
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import br.edu.utfpr.trabalhoconclusaocurso.services.LocationService
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var usuario: Usuario? = null
    private lateinit var btnIniciarParar: Button
    private var isTracking = false
    private val polylineOptions = PolylineOptions().width(10f).color(android.graphics.Color.BLUE)

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

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        usuario = intent?.getSerializableExtra("usuario") as? Usuario
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Configurar o mapa conforme necessário
        map.moveCamera(CameraUpdateFactory.zoomTo(22f))
        map.moveCamera(CameraUpdateFactory.newLatLng(
            com.google.android.gms.maps.model.LatLng(
                -23.550520,
                -46.633308
            )
        ))
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
        registerReceiver(locationReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(locationReceiver)
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "LOCATION_UPDATE") {
                val lat = intent.getDoubleExtra("latitude", 0.0)
                val lon = intent.getDoubleExtra("longitude", 0.0)
                val distancia = intent.getDoubleExtra("distancia", 0.0)

                val ponto = LatLng(lat, lon)

                // Adiciona o ponto na rota
                polylineOptions.add(ponto)
                map.clear()
                map.addPolyline(polylineOptions)
                map.addMarker(MarkerOptions().position(ponto).title("Posição atual"))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(ponto, 17f))
            }
        }
    }
}