package br.edu.utfpr.trabalhoconclusaocurso.data.repository

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import br.edu.utfpr.trabalhoconclusaocurso.data.dao.UsuarioDao
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Atividade
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
            firebase.document(usuario.id!!).set(usuario).await()
            Log.d("FIREBASE", "Usuário salvo com sucesso no Firestore: ${usuario.id}")
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao salvar no Firestore", e)
        }
    }

    suspend fun atualizar(usuario: Usuario) {
        usuarioDao.atualizar(usuario)

        firebase.document(usuario.id!!)
            .set(usuario)
            .await()
    }

    // Busca no SQLite
    fun listarLocal(): List<Usuario> = usuarioDao.listarTodos()

    // Sincroniza do Firebase para SQLite
    suspend fun sincronizar() {
        try {
            // Firebase → SQLite
            val snapshot = firebase.get().await()
            snapshot.documents.forEach { doc ->
                val usuario = doc.toObject(Usuario::class.java)
                usuario?.let {
                    val existente = usuarioDao.buscarPorId(it.id!!)
                    if (existente == null) {
                        usuarioDao.inserir(it)
                    } else {
                        usuarioDao.atualizar(it)
                    }
                }
            }

            // SQLite → Firebase
            val usuariosLocais = usuarioDao.listarTodos()
            usuariosLocais.forEach { usuario ->
                firebase.document(usuario.id!!).set(usuario).await()
            }

        } catch (e: Exception) {
            Log.e("SYNC", "Erro ao sincronizar", e)
        }
    }
}