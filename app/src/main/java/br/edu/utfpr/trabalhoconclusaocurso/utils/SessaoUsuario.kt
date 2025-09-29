package br.edu.utfpr.trabalhoconclusaocurso.utils

import android.content.Context
import br.edu.utfpr.trabalhoconclusaocurso.App
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario

object SessaoUsuario {
    private lateinit var usuario: Usuario
    private var isLoggedIn: Boolean = false

    fun login(usuario: Usuario) {
        this.usuario = usuario
        this.isLoggedIn = true
        salvarSessaoNoSharedPreferences(usuario)
    }

    fun logout() {
        this.isLoggedIn = false
        limparSessaoDoSharedPreferences()
    }

    fun estaLogado(): Boolean {
        return isLoggedIn && this::usuario.isInitialized
    }

    fun getUsuario(): Usuario? {
        return if (estaLogado()) usuario else null
    }

    fun getUsuarioId(): String? = getUsuario()?.id
    fun getUsuarioNome(): String? = getUsuario()?.nome
    fun getUsuarioUsername(): String? = getUsuario()?.username
    fun getUsuarioPeso(): Float? = getUsuario()?.peso?.toFloat()
    fun getUsuarioAltura(): Float? = getUsuario()?.altura?.toFloat()
    fun getUsuarioCPF(): String? = getUsuario()?.cpf
    fun getUsuarioDistanciaPreferida(): Float? = getUsuario()?.distanciaPreferida?.toFloat()
    fun getUsuarioIdade(): Int? = getUsuario()?.idade
    fun getUsuarioSenha(): String? = getUsuario()?.usuarioSenha

    private fun salvarSessaoNoSharedPreferences(usuario: Usuario) {
        val prefs = App.instance.getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putBoolean("isLoggedIn", true)
        editor.putString("usuarioId", usuario.id)
        editor.putString("usuarioNome", usuario.nome)
        editor.putString("usuarioUsername", usuario.username)
        editor.putString("usuarioCPF", usuario.cpf)
        editor.putFloat("usuarioPeso", usuario.peso?.toFloat() ?: 0f)
        editor.putFloat("usuarioAltura", usuario.altura?.toFloat() ?: 0f)
        editor.putFloat("usuarioDistanciaPreferida", usuario.distanciaPreferida?.toFloat() ?: 0f)
        editor.putInt("usuarioIdade", usuario.idade ?: 0)
        editor.putString("usuarioSenha", usuario.usuarioSenha)

        editor.apply()
    }

    private fun limparSessaoDoSharedPreferences() {
        val prefs = App.instance.getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun carregarSessaoDoSharedPreferences(): Boolean {
        val prefs = App.instance.getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)

        if (prefs.getBoolean("isLoggedIn", false)) {
            val usuario = Usuario().apply {
                id = prefs.getString("usuarioId", "")
                nome = prefs.getString("usuarioNome", "")
                username = prefs.getString("usuarioUsername", "")
                cpf = prefs.getString("usuarioCPF", "")
                peso = prefs.getFloat("usuarioPeso", 0f).toDouble()
                altura = prefs.getFloat("usuarioAltura", 0f).toDouble()
                distanciaPreferida = prefs.getFloat("usuarioDistanciaPreferida", 0f).toDouble()
                idade = prefs.getInt("usuarioIdade", 0)
                usuarioSenha = prefs.getString("usuarioSenha", "")
            }

            this.usuario = usuario
            this.isLoggedIn = true
            return true
        }

        return false
    }
}
