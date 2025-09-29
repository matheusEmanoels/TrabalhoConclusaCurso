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

    fun getUsuarioId(): String? {
        return getUsuario()?.id
    }

    fun getUsuarioNome(): String? {
        return getUsuario()?.nome
    }

    fun getUsuarioUsername(): String? {
        return getUsuario()?.username
    }

    fun getUsuarioPeso(): Float? {
        return getUsuario()?.peso?.toFloat()

    }

    private fun salvarSessaoNoSharedPreferences(usuario: Usuario) {
        val prefs = App.instance.getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putBoolean("isLoggedIn", true)
        editor.putString("usuarioId", usuario.id)
        editor.putString("usuarioNome", usuario.nome)
        editor.putString("usuarioUsername", usuario.username)
        editor.putFloat("usuarioPeso", usuario.peso!!.toFloat())
        editor.putFloat("usuarioDistanciaPreferida", usuario.distanciaPreferida!!.toFloat())

        editor.apply()
    }

    private fun limparSessaoDoSharedPreferences() {
        val prefs = App.instance.getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun carregarSessaoDoSharedPreferences(): Boolean {
        val prefs = App.instance.getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)

        if (prefs.getBoolean("isLoggedIn", false)) {
            val usuarioId = prefs.getString("usuarioId", "")
            val usuarioNome = prefs.getString("usuarioNome", "") ?: ""
            val usuarioUsername = prefs.getString("usuarioUsername", "") ?: ""
            val usuarioPeso = prefs.getFloat("usuarioPeso", 0f)
            val usuarioDistanciaPreferida = prefs.getFloat("usuarioDistanciaPreferida", 0f)

            val usuario = Usuario().apply {
                this.id = usuarioId
                this.nome = usuarioNome
                this.username = usuarioUsername
                this.peso = usuarioPeso.toDouble()
                this.distanciaPreferida = usuarioDistanciaPreferida.toDouble()
            }

            this.usuario = usuario
            this.isLoggedIn = true
            return true
        }

        return false
    }
}