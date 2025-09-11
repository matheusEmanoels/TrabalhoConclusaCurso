package br.edu.utfpr.trabalhoconclusaocurso.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.trabalhoconclusaocurso.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun OnClickLogin(view: View) {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    fun OnClickCadastrar(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}