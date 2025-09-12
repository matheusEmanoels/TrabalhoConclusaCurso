package br.edu.utfpr.trabalhoconclusaocurso.activities

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.trabalhoconclusaocurso.R
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import br.edu.utfpr.trabalhoconclusaocurso.data.repository.UsuarioRepository
import br.edu.utfpr.trabalhoconclusaocurso.services.DBHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class RegisterActivity : AppCompatActivity() {
    private lateinit var repository: UsuarioRepository
    private lateinit var db: SQLiteDatabase
    private lateinit var etNome: EditText
    private lateinit var etNomeUsuario: EditText
    private lateinit var etCpf: EditText
    private lateinit var etSenha: EditText
    private lateinit var etPeso: EditText
    private lateinit var etAltura: EditText
    private lateinit var etIdade: EditText
    private lateinit var etDistancia: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = DBHelper(this).writableDatabase
        repository = UsuarioRepository(db)

        etNome = findViewById<EditText>(R.id.etNome)
        etNomeUsuario = findViewById<EditText>(R.id.etNomeUsuario)
        etCpf = findViewById<EditText>(R.id.etCpf)
        etSenha = findViewById<EditText>(R.id.etSenha)
        etPeso = findViewById<EditText>(R.id.etPeso)
        etAltura = findViewById<EditText>(R.id.etAltura)
        etDistancia = findViewById<EditText>(R.id.etDistancia)
        etIdade = findViewById<EditText>(R.id.etIdade)
    }

    fun OnClickCriarConta(view: View) {
        val usuario = Usuario(
            id = UUID.randomUUID().toString(),
            nome = etNome.text.toString(),
            username = etNomeUsuario.text.toString(),
            cpf = etCpf.text.toString(),
            idade = etIdade.text.toString().toIntOrNull() ?: 0,
            altura = etAltura.text.toString().toDoubleOrNull() ?: 0.0,
            peso = etPeso.text.toString().toDoubleOrNull() ?: 0.0,
            distanciaPreferida = etDistancia.text.toString().toDoubleOrNull() ?: 0.0,
            usuarioSenha = hashSenha(etSenha.text.toString())
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.salvar(usuario)
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Usuário cadastrado!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun hashSenha(senha: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(senha.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}