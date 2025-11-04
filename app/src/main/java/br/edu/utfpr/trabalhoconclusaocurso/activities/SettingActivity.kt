package br.edu.utfpr.trabalhoconclusaocurso.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import br.edu.utfpr.trabalhoconclusaocurso.R
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.AtividadeRepository
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.CoordenadaRepository
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.UsuarioRepository
import br.edu.utfpr.trabalhoconclusaocurso.services.DBHelper
import br.edu.utfpr.trabalhoconclusaocurso.utils.SessaoUsuario
import com.google.android.gms.location.LocationCallback
import kotlinx.coroutines.launch


class SettingsActivity : AppCompatActivity() {
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var atividadeRepository: AtividadeRepository
    private lateinit var coordenadaRepository: CoordenadaRepository
    private lateinit var usuario: Usuario
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preferences)

        val seekBarFrequencia = findViewById<SeekBar>(R.id.seekBarFrequencia)
        val tvFrequenciaValue = findViewById<TextView>(R.id.tvFrequenciaValue)
        val seekBarDistancia = findViewById<SeekBar>(R.id.seekBarDistancia)
        val tvDistanciaValue = findViewById<TextView>(R.id.tvDistanciaValue)
        val checkFeedback = findViewById<CheckBox>(R.id.checkFeedback)
        val btnSave = findViewById<Button>(R.id.btnSaveConfig)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnSync = findViewById<Button>(R.id.btnSync)

        dbHelper = DBHelper(this)
        usuarioRepository = UsuarioRepository(dbHelper.writableDatabase)
        atividadeRepository = AtividadeRepository(dbHelper.writableDatabase)
        coordenadaRepository = CoordenadaRepository(dbHelper.writableDatabase)
        usuario = SessaoUsuario.getUsuario()!!

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        seekBarFrequencia.progress = prefs.getInt(KEY_FREQUENCIA_ATUALIZACAO, 2)
        tvFrequenciaValue.text = "${seekBarFrequencia.progress}s"

        seekBarDistancia.progress = prefs.getInt(KEY_DISTANCIA_DESEJADA, 10)
        tvDistanciaValue.text = "${usuario.distanciaPreferida}km"

        checkFeedback.isChecked = prefs.getBoolean(KEY_FEEDBACK_AUDIO, true)

        seekBarFrequencia.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvFrequenciaValue.text = "${progress}s"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarDistancia.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDistanciaValue.text = "${progress}km"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSave.setOnClickListener {
            prefs.edit()
                .putInt(KEY_FREQUENCIA_ATUALIZACAO, seekBarFrequencia.progress)
                .putInt(KEY_DISTANCIA_DESEJADA, seekBarDistancia.progress)
                .putBoolean(KEY_FEEDBACK_AUDIO, checkFeedback.isChecked)
                .apply()
            finish()
            usuario.distanciaPreferida = seekBarDistancia.progress.toDouble()

            lifecycleScope.launch {
                usuarioRepository.atualizar(usuario)
            }

            Toast.makeText(this, "Configurações salvas", Toast.LENGTH_SHORT).show()
        }

        btnSync.setOnClickListener {
            if (!isOnline()) {
                Toast.makeText(this, "Sem conexão com a internet, não foi possivel sincronizar dados", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            showLoading(true) // mostra overlay

            lifecycleScope.launch {
                try {
                    sincronizarAll(usuario.id!!)
                    Toast.makeText(this@SettingsActivity, "Sincronização concluída!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@SettingsActivity, "Erro ao sincronizar!", Toast.LENGTH_SHORT).show()
                    Log.e("SYNC", "Erro ao sincronizar tudo", e)
                } finally {
                    showLoading(false)
                }
            }
        }

        btnLogout.setOnClickListener {
            SessaoUsuario.logout()
            redirectToMap()
        }
    }

    companion object Config {
        private const val KEY_FREQUENCIA_ATUALIZACAO = "frequencia_atualizacao"
        private const val KEY_DISTANCIA_DESEJADA = "distancia_desejada"
        private const val KEY_FEEDBACK_AUDIO = "feedback_audio"

        fun getFrequenciaAtualizacao(context: Context): Int {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getInt(KEY_FREQUENCIA_ATUALIZACAO, 10)
        }

        fun getDistanciaDesejada(context: Context): Int {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getInt(KEY_DISTANCIA_DESEJADA, 100)
        }

        fun isFeedbackAudioLigado(context: Context): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getBoolean(KEY_FEEDBACK_AUDIO, true)
        }
    }

    private fun redirectToMap() {
        val intent = Intent(this, MapActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun sincronizarAll(idUsuario: String) {
        try {
            atividadeRepository.sincronizar(idUsuario)

            val atividades = atividadeRepository.listarPorUsuarioLocal(idUsuario)
            atividades.forEach { atividade ->
                coordenadaRepository.sincronizar(idUsuario, atividade.id!!)
            }

        } catch (e: Exception) {
            Log.e("SYNC", "Erro ao sincronizar tudo", e)
            throw e
        }
    }

    private fun showLoading(show: Boolean) {
        val layoutLoading = findViewById<FrameLayout>(R.id.layoutLoading)
        layoutLoading.visibility = if (show) View.VISIBLE else View.GONE
    }
}