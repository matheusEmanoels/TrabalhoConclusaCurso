package br.edu.utfpr.trabalhoconclusaocurso.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.trabalhoconclusaocurso.R
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.UsuarioRepository
import br.edu.utfpr.trabalhoconclusaocurso.services.DBHelper
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var userRepo: UsuarioRepository
    private lateinit var dbHelper: DBHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)

        userRepo = UsuarioRepository(dbHelper.writableDatabase)
        lifecycleScope.launch {
            userRepo.sincronizar()
        }
    }

    fun OnClickLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
    fun OnClickRegister(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}


