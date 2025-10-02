package br.edu.utfpr.trabalhoconclusaocurso.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.trabalhoconclusaocurso.R
import br.edu.utfpr.trabalhoconclusaocurso.fragments.HistoricFragment
import br.edu.utfpr.trabalhoconclusaocurso.fragments.RelFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class RelatoriosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorios)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val tvTitulo = findViewById<TextView>(R.id.tvTitulo)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, HistoricFragment())
            .commit()

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_historico -> {
                    tvTitulo.setText("Historico")
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameContainer, HistoricFragment())
                        .commit()
                    true
                }
                R.id.nav_relatorios -> {
                    tvTitulo.setText("Graficos de Evolução")
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameContainer, RelFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}