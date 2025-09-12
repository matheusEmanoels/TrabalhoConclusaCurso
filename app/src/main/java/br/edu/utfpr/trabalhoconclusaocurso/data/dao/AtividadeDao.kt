package br.edu.utfpr.trabalhoconclusaocurso.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Atividade

class AtividadeDao(private val db: SQLiteDatabase) {

    fun inserir(atividade: Atividade): Long {
        val values = ContentValues().apply {
            put("id", atividade.id)
            put("id_usuario", atividade.idUsuario)
            put("nome", atividade.nome)
            put("data_hora", atividade.dataHora)
            put("duracao", atividade.duracao)
            put("distancia", atividade.distancia)
            put("velocidade_media", atividade.velocidadeMedia)
            put("calorias_perdidas", atividade.caloriasPerdidas)
        }
        return db.insert("Atividade", null, values)
    }

    fun atualizar(atividade: Atividade) {
        val values = ContentValues().apply {
            put("duracao", atividade.duracao)
            put("distancia", atividade.distancia)
            put("velocidade_media", atividade.velocidadeMedia)
            put("calorias_perdidas", atividade.caloriasPerdidas)
        }
        db.update("Atividade", values, "id = ?", arrayOf(atividade.id))
    }

    fun listarPorUsuario(idUsuario: String): List<Atividade> {
        val lista = mutableListOf<Atividade>()
        val cursor: Cursor = db.query(
            "Atividade", null, "id_usuario=?", arrayOf(idUsuario), null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    Atividade(
                        id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        idUsuario = cursor.getString(cursor.getColumnIndexOrThrow("id_usuario")),
                        nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                        dataHora = cursor.getString(cursor.getColumnIndexOrThrow("data_hora")),
                        duracao = cursor.getLong(cursor.getColumnIndexOrThrow("duracao")),
                        distancia = cursor.getDouble(cursor.getColumnIndexOrThrow("distancia")),
                        velocidadeMedia = cursor.getDouble(cursor.getColumnIndexOrThrow("velocidade_media")),
                        caloriasPerdidas = cursor.getDouble(cursor.getColumnIndexOrThrow("calorias_perdidas"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }
}