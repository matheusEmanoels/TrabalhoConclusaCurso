package br.edu.utfpr.trabalhoconclusaocurso.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.trabalhoconclusaocurso.R
import br.edu.utfpr.trabalhoconclusaocurso.data.dao.UsuarioDao
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.UsuarioRepository
import br.edu.utfpr.trabalhoconclusaocurso.services.DBHelper
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var usuarioDao: UsuarioDao
    private lateinit var dbHelper: DBHelper
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)

        dbHelper = DBHelper(this)
        usuarioDao = UsuarioDao(dbHelper.writableDatabase)
        usuarioRepository = UsuarioRepository(dbHelper.writableDatabase)
    }

    fun OnClickLogin(view: View) {

        if (etUsername.text.isEmpty() || etPassword.text.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
        }

        login(etUsername.text.toString(), etPassword.text.toString())
    }

    fun OnClickCadastrar(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun login(username: String, senha: String) {
        val senhaHash = hashSenha(senha)

        val usuarioLocal = usuarioDao.buscarPorUsername(username)
        if (usuarioLocal != null) {
            if (usuarioLocal.usuarioSenha == senhaHash) {
                Toast.makeText(this, "Login local bem-sucedido!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
                return
            } else {
                Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show()
                return
            }
        }
    }

    fun hashSenha(senha: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(senha.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}