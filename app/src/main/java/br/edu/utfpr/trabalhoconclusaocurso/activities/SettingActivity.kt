package br.edu.utfpr.trabalhoconclusaocurso.activities

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import br.edu.utfpr.trabalhoconclusaocurso.R
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.UsuarioRepository
import br.edu.utfpr.trabalhoconclusaocurso.services.DBHelper
import com.google.android.gms.location.LocationCallback
import kotlinx.coroutines.launch


class SettingsActivity : AppCompatActivity() {
    private lateinit var usuarioRepository: UsuarioRepository
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

        dbHelper = DBHelper(this)
        usuarioRepository = UsuarioRepository(dbHelper.writableDatabase)
        usuario = (intent?.getSerializableExtra("usuario") as? Usuario)!!

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        seekBarFrequencia.progress = prefs.getInt(KEY_FREQUENCIA_ATUALIZACAO, 10)
        tvFrequenciaValue.text = "${seekBarFrequencia.progress}s"

        seekBarDistancia.progress = prefs.getInt(KEY_DISTANCIA_DESEJADA, 100)
        tvDistanciaValue.text = "${seekBarDistancia.progress}m"

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
}