package br.edu.utfpr.trabalhoconclusaocurso.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.edu.utfpr.trabalhoconclusaocurso.R
import com.google.android.gms.location.FusedLocationProviderClient
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
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.AtividadeRepository
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.CoordenadaRepository
import br.edu.utfpr.trabalhoconclusaocurso.services.DBHelper
import br.edu.utfpr.trabalhoconclusaocurso.services.LocationService
import br.edu.utfpr.trabalhoconclusaocurso.utils.SessaoUsuario
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.UUID

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnIniciarParar: Button
    private lateinit var btnRelatorios: ExtendedFloatingActionButton
    private lateinit var btnConfiguracoes: ExtendedFloatingActionButton
    private lateinit var btnOk: Button
    private lateinit var btnMore : FloatingActionButton
    private lateinit var cvResumo : CardView
    private lateinit var tvDistancia: TextView
    private lateinit var tvPace: TextView
    private lateinit var tvCalorias: TextView
    private lateinit var tvDuracao: TextView
    private lateinit var atividadeId: String
    private lateinit var dbHelper: DBHelper
    private lateinit var coordenadaRepository: CoordenadaRepository
    private lateinit var atividadeRepository: AtividadeRepository
    private var isTracking = false
    private var polylineOptions = PolylineOptions().width(10f).color(android.graphics.Color.BLUE)
    private var primeiraPosicao = true
    private var isFabOpen = true
    private val pontosPercurso = mutableListOf<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        dbHelper = DBHelper(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        coordenadaRepository = CoordenadaRepository(dbHelper.writableDatabase)
        atividadeRepository = AtividadeRepository(dbHelper.writableDatabase)

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

        if (!SessaoUsuario.estaLogado()) {
            redirectToLogin()
            return
        }

        btnIniciarParar = findViewById(R.id.btn_start_stop)
        btnIniciarParar.setBackgroundColor(getColor(R.color.success))
        btnRelatorios = findViewById(R.id.btn_rel)
        btnConfiguracoes = findViewById(R.id.btn_settings)
        btnMore = findViewById(R.id.fab_main)
        btnOk = findViewById(R.id.btn_ok)
        btnOk.setBackgroundColor(getColor(R.color.success))
        cvResumo = findViewById(R.id.info_panel)
        tvDistancia = findViewById(R.id.tv_distancia)
        tvPace = findViewById(R.id.tv_pace)
        tvCalorias = findViewById(R.id.tv_calorias)
        tvDuracao = findViewById(R.id.tv_duracao)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val usuario = SessaoUsuario.getUsuario()
        usuario?.let {
            Toast.makeText(this, "Bem-vindo, ${it.username}!", Toast.LENGTH_SHORT).show()
        }

        isTracking = carregarFlag(this)

        val atividadeIdHistorico = intent.getStringExtra("ATIVIDADE_ID")
        if (atividadeIdHistorico != null) {
            isTracking = false
            btnIniciarParar.visibility = View.GONE
            btnMore.visibility = View.GONE

            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                map = googleMap
                carregarHistorico(atividadeIdHistorico)
            }
        } else {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
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
            atividadeId = UUID.randomUUID().toString()
            val serviceIntent = Intent(this, LocationService::class.java)
            serviceIntent.putExtra("atividadeId", atividadeId)
            startService(serviceIntent)
        }
        isTracking = !isTracking
        salvarFlag(this, isTracking)
    }

    fun OnClickConfiguracoes(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun OnClickRelatorios(view: View) {
        val intent = Intent(this, RelatoriosActivity::class.java)
        startActivity(intent)
    }

    fun OnMoreClick(view: View) {
        if (isFabOpen) {
            btnRelatorios.hide()
            btnConfiguracoes.hide()
        } else {
            btnRelatorios.show()
            btnConfiguracoes.show()
        }
        isFabOpen = !isFabOpen
    }

    override fun onResume() {
        super.onResume()
        val locationFilter = IntentFilter("LOCATION_UPDATE")
        ContextCompat.registerReceiver(
            this,
            locationReceiver,
            locationFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
        val finalFilter = IntentFilter("FINAL_UPDATE")
        ContextCompat.registerReceiver(
            this,
            finalReceiver,
            finalFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

        OnMoreClick(this.btnMore)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(locationReceiver)
        unregisterReceiver(finalReceiver)
    }

    fun OnClickOk(view: View) {
        cvResumo.visibility = View.GONE
        map.clear()
        polylineOptions = PolylineOptions().width(10f).color(android.graphics.Color.BLUE) // Reinicia a linha
        tvDistancia.text = "Distância: 0.00 km"
        tvPace.text = "Pace Médio: 0'00\" /km"
        tvCalorias.text = "Calorias: 0 kcal"
        tvDuracao.text = "Duração: 00:00:00"
        btnIniciarParar.visibility = View.VISIBLE
        btnMore.visibility = View.VISIBLE
        pontosPercurso.clear()
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "LOCATION_UPDATE") {
                val lat = intent.getDoubleExtra("latitude", 0.0)
                val lon = intent.getDoubleExtra("longitude", 0.0)

                val ponto = LatLng(lat, lon)
                pontosPercurso.add(ponto)

                polylineOptions.add(ponto)
                map.clear()
                map.addPolyline(polylineOptions)
                map.addMarker(MarkerOptions().position(ponto).title("Posição atual"))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(ponto, 17f))

                val distancia = intent.getDoubleExtra("distancia", 0.0)
                val paceMin = intent.getIntExtra("paceMin", 0)
                val paceSec = intent.getIntExtra("paceSec", 0)
                val calorias = intent.getDoubleExtra("calorias", 0.0)
                val duracao = intent.getDoubleExtra("duracao", 0.0)

                tvDistancia.text = "Distância: %.2f km".format(distancia / 1000)
                tvPace.text = "Pace Médio: %d'%02d\" /km".format(paceMin, paceSec)
                tvCalorias.text = "Calorias: %.0f kcal".format(calorias)

                val horas = (duracao / 3600).toInt()
                val minutos = ((duracao % 3600) / 60).toInt()
                val segundos = (duracao % 60).toInt()
                tvDuracao.text = "Duração: %02d:%02d:%02d".format(horas, minutos, segundos)
            }
        }
    }

    private val finalReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "FINAL_UPDATE"){
                tvDistancia.text = "Distância: %.2f km".format(intent.getDoubleExtra("distancia", 0.0))
                tvPace.text = "Pace Médio: %d'%02d\" /km".format(intent.getIntExtra("paceMin", 0), intent.getIntExtra("paceSec", 0))
                tvCalorias.text = "Calorias: %.0f kcal".format(intent.getDoubleExtra("calorias", 0.0))

                val duracao = intent.getDoubleExtra("duracao", 0.0)
                val horas = (duracao / 3600).toInt()
                val minutos = ((duracao % 3600) / 60).toInt()
                val segundos = (duracao % 60).toInt()

                tvDuracao.text = "Duração: %02d:%02d:%02d".format(horas, minutos, segundos)

                cvResumo.visibility = View.VISIBLE
                btnIniciarParar.visibility = View.GONE
                btnRelatorios.visibility = View.GONE
                btnConfiguracoes.visibility = View.GONE

                if (pontosPercurso.isNotEmpty()) {
                    val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.Builder()
                    for (p in pontosPercurso) {
                        boundsBuilder.include(p)
                    }
                    val bounds = boundsBuilder.build()
                    val padding = 100
                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                    map.animateCamera(cameraUpdate)
                }
            }
        }
    }

    fun carregarHistorico(atividadeId: String) {
        val coordenadas = coordenadaRepository.listarLocal(atividadeId)
        val atividade = atividadeRepository.buscarPorId(atividadeId)
        polylineOptions = PolylineOptions().width(10f).color(android.graphics.Color.BLUE)

        pontosPercurso.clear()
        for (coord in coordenadas) {
            val ponto = LatLng(coord.latitude, coord.longitude)
            polylineOptions.add(ponto)
            pontosPercurso.add(ponto)
        }

        map.clear()
        map.addPolyline(polylineOptions)

        if (pontosPercurso.isNotEmpty()) {
            val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.Builder()
            for (p in pontosPercurso) {
                boundsBuilder.include(p)
            }
            val bounds = boundsBuilder.build()
            val padding = 100
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            map.animateCamera(cameraUpdate)
        }

        atividade?.let {
            val pace = if (atividade?.distancia!! > 0) (atividade.duracao / 60.0) / (atividade.distancia / 1000.0) else 0.0
            val paceMin = pace.toInt()
            val paceSec = ((pace - paceMin) * 60).toInt()
            tvDistancia.text = "Distância: %.2f km".format(it.distancia / 1000)
            tvPace.text = "Pace Médio: %d'%02d\" /km".format(paceMin, paceSec)
            tvCalorias.text = "Calorias: %.0f kcal".format(it.caloriasPerdidas)

            val duracao = it.duracao
            val horas = (duracao / 3600).toInt()
            val minutos = ((duracao % 3600) / 60).toInt()
            val segundos = (duracao % 60).toInt()
            tvDuracao.text = "Duração: %02d:%02d:%02d".format(horas, minutos, segundos)

            cvResumo.visibility = View.VISIBLE
        }

        if (pontosPercurso.isNotEmpty()) {
            val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.Builder()
            for (p in pontosPercurso) {
                boundsBuilder.include(p)
            }
            val bounds = boundsBuilder.build()
            val padding = 100
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            map.animateCamera(cameraUpdate)
        }

        btnIniciarParar.visibility = View.GONE
        btnRelatorios.visibility = View.GONE
        btnConfiguracoes.visibility = View.GONE
        btnMore.visibility = View.GONE
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    fun salvarFlag(context: Context, isTracking: Boolean) {
        val sharedPref = context.getSharedPreferences("IsTracking", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("IS_TRACKING", isTracking).apply()
    }

    fun carregarFlag(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences("IsTracking", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("IS_TRACKING", false)
    }
}