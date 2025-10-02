package br.edu.utfpr.trabalhoconclusaocurso.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Coordenada

class CoordenadaDao(private val db: SQLiteDatabase) {

    fun inserir(coord: Coordenada): Long {
        val values = ContentValues().apply {
            put("id", coord.id)
            put("id_atividade", coord.idAtividade)
            put("latitude", coord.latitude)
            put("longitude", coord.longitude)
        }
        return db.insert("Coordenada", null, values)
    }

    fun atualizar(coord: Coordenada) {
        val values = ContentValues().apply {
            put("latitude", coord.latitude)
            put("longitude", coord.longitude)
        }
        db.update("Coordenada", values, "id = ?", arrayOf(coord.id))
    }

    fun listarPorAtividade(idAtividade: String): List<Coordenada> {
        val lista = mutableListOf<Coordenada>()
        val cursor: Cursor = db.query(
            "Coordenada", null, "id_atividade=?", arrayOf(idAtividade), null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    Coordenada(
                        id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        idAtividade = cursor.getString(cursor.getColumnIndexOrThrow("id_atividade")),
                        latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                        longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun buscarPorId(id: String): Coordenada? {
        val cursor = db.query(
            "Coordenada", null, "id=?", arrayOf(id), null, null, null
        )
        var coordenada: Coordenada? = null

        if (cursor.moveToFirst()) {
            coordenada = Coordenada(
                id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                idAtividade = cursor.getString(cursor.getColumnIndexOrThrow("id_atividade")),
                latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"))
            )
        }
        cursor.close()
        return coordenada
    }
}
