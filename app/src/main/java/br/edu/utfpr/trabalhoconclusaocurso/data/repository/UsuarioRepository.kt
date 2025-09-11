package br.edu.utfpr.trabalhoconclusaocurso.data.repository

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import br.edu.utfpr.trabalhoconclusaocurso.data.dao.UsuarioDao
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsuarioRepository(private val db: SQLiteDatabase) {

    private val usuarioDao = UsuarioDao(db)
    private val firebase = FirebaseFirestore.getInstance().collection("usuarios")

    // Salva no SQLite e Firebase
    suspend fun salvar(usuario: Usuario) {
        try {
            usuarioDao.inserir(usuario)
            firebase.document(usuario.id).set(usuario).await()
            Log.d("FIREBASE", "Usuário salvo com sucesso no Firestore: ${usuario.id}")
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao salvar no Firestore", e)
        }
    }

    // Busca no SQLite
    fun listarLocal(): List<Usuario> = usuarioDao.listarTodos()

    // Sincroniza do Firebase para SQLite
    suspend fun sincronizar() {
        val snapshot = firebase.get().await()
        snapshot.documents.forEach { doc ->
            val usuario = doc.toObject(Usuario::class.java)
            usuario?.let { usuarioDao.inserir(it) }
        }
    }
}