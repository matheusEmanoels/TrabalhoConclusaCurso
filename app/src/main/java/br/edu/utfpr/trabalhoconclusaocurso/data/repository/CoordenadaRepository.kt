package br.edu.utfpr.trabalhoconclusaocurso.data.repository

import android.database.sqlite.SQLiteDatabase
import br.edu.utfpr.trabalhoconclusaocurso.data.dao.CoordenadaDao
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Coordenada
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CoordenadaRepository(private val db: SQLiteDatabase) {

    private val coordenadaDao = CoordenadaDao(db)
    private val firebase = FirebaseFirestore.getInstance().collection("usuarios")

    suspend fun salvar(coord: Coordenada, idUsuario: String) {
        coordenadaDao.inserir(coord)

        firebase.document(idUsuario)
            .collection("atividades")
            .document(coord.idAtividade)
            .collection("coordenadas")
            .add(coord)
            .await()
    }

    fun listarLocal(idAtividade: String): List<Coordenada> =
        coordenadaDao.listarPorAtividade(idAtividade)

    suspend fun sincronizar(idUsuario: String, idAtividade: String) {
        val snapshot = firebase.document(idUsuario)
            .collection("atividades")
            .document(idAtividade)
            .collection("coordenadas")
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            val coordenada = doc.toObject(Coordenada::class.java)
            coordenada?.let { coordenadaDao.inserir(it) }
        }
    }
}