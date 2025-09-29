package br.edu.utfpr.trabalhoconclusaocurso

import android.app.Application
import android.content.Context
import br.edu.utfpr.trabalhoconclusaocurso.utils.SessaoUsuario

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        SessaoUsuario.carregarSessaoDoSharedPreferences()
    }

    fun getAppContext(): Context = applicationContext
}