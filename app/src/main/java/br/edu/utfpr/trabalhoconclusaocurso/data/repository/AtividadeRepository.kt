package br.edu.utfpr.trabalhoconclusaocurso.data.repository

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import br.edu.utfpr.trabalhoconclusaocurso.data.dao.AtividadeDao
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Atividade
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AtividadeRepository(private val db: SQLiteDatabase) {

    private val atividadeDao = AtividadeDao(db)
    private val firebase = FirebaseFirestore.getInstance().collection("usuarios")

    suspend fun salvar(atividade: Atividade) {
        atividadeDao.inserir(atividade)

        // Atividade vai como subcoleção de Usuario
        firebase.document(atividade.idUsuario)
            .collection("atividades")
            .document(atividade.id)
            .set(atividade)
            .await()
    }

    suspend fun atualizar(atividade: Atividade) {
        atividadeDao.atualizar(atividade)

        firebase.document(atividade.idUsuario)
            .collection("atividades")
            .document(atividade.id)
            .set(atividade)
            .await()
    }

    fun excluir(atividade: Atividade, onComplete: (Boolean) -> Unit) {
        atividadeDao.excluir(atividade.id!!)

        firebase.document(atividade.idUsuario)
            .collection("atividades")
            .document(atividade.id)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun listarPorUsuarioLocal(idUsuario: String): List<Atividade> =
        atividadeDao.listarPorUsuario(idUsuario)

    fun buscarPorId(idAtividade: String) : Atividade?{
        return atividadeDao.buscarPorId(idAtividade)
    }

    suspend fun sincronizar(idUsuario: String) {
        try {
            // Firebase → SQLite
            val snapshot = firebase.document(idUsuario)
                .collection("atividades")
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val atividade = doc.toObject(Atividade::class.java)
                atividade?.let {
                    val existente = atividadeDao.buscarPorId(it.id!!)
                    if (existente == null) {
                        atividadeDao.inserir(it)
                    } else {
                        atividadeDao.atualizar(it)
                    }
                }
            }

            // SQLite → Firebase
            val atividadesLocais = atividadeDao.listarPorUsuario(idUsuario)
            atividadesLocais.forEach { atividade ->
                firebase.document(idUsuario)
                    .collection("atividades")
                    .document(atividade.id!!)
                    .set(atividade)
                    .await()
            }

        } catch (e: Exception) {
            Log.e("SYNC", "Erro ao sincronizar atividades", e)
        }
    }
}