package br.edu.utfpr.trabalhoconclusaocurso.data.repository

import android.database.sqlite.SQLiteDatabase
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

    fun listarPorUsuarioLocal(idUsuario: String): List<Atividade> =
        atividadeDao.listarPorUsuario(idUsuario)

    suspend fun sincronizar(idUsuario: String) {
        val snapshot = firebase.document(idUsuario)
            .collection("atividades")
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            val atividade = doc.toObject(Atividade::class.java)
            atividade?.let { atividadeDao.inserir(it) }
        }
    }
}