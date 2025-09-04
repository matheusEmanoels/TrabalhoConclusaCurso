package br.edu.utfpr.trabalhoconclusaocurso.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.trabalhoconclusaocurso.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.type.LatLng

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var btnIniciarParar: Button
    private var isTracking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Configura o mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        btnIniciarParar = findViewById(R.id.btn_start_stop)
        btnIniciarParar.setBackgroundColor(getColor(R.color.success))

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
        btnIniciarParar.text = if (isTracking) "Iniciar" else "Parar"
        btnIniciarParar.setBackgroundColor(if (isTracking) getColor(R.color.success) else getColor(R.color.error))
        isTracking = !isTracking
    }
    fun OnClickConfiguracoes(view: View) {}
}