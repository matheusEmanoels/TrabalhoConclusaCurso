package br.edu.utfpr.trabalhoconclusaocurso.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario

class UsuarioDao(private val db: SQLiteDatabase) {

    fun inserir(usuario: Usuario): Long {
        val values = ContentValues().apply {
            put("id", usuario.id)
            put("nome", usuario.nome)
            put("cpf", usuario.cpf)
            put("idade", usuario.idade)
            put("altura", usuario.altura)
            put("peso", usuario.peso)
            put("distancia_preferida", usuario.distanciaPreferida)
            put("usuario_senha", usuario.usuarioSenha)
        }
        return db.insert("Usuario", null, values)
    }

    fun listarTodos(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        val cursor: Cursor = db.query("Usuario", null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    Usuario(
                        id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                        cpf = cursor.getString(cursor.getColumnIndexOrThrow("cpf")),
                        idade = cursor.getInt(cursor.getColumnIndexOrThrow("idade")),
                        altura = cursor.getDouble(cursor.getColumnIndexOrThrow("altura")),
                        peso = cursor.getDouble(cursor.getColumnIndexOrThrow("peso")),
                        distanciaPreferida = cursor.getDouble(cursor.getColumnIndexOrThrow("distancia_preferida")),
                        usuarioSenha = cursor.getString(cursor.getColumnIndexOrThrow("usuario_senha"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun buscarPorId(id: String): Usuario? {
        val cursor = db.query(
            "Usuario", null, "id=?", arrayOf(id), null, null, null
        )
        return if (cursor.moveToFirst()) {
            Usuario(
                id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                cpf = cursor.getString(cursor.getColumnIndexOrThrow("cpf")),
                idade = cursor.getInt(cursor.getColumnIndexOrThrow("idade")),
                altura = cursor.getDouble(cursor.getColumnIndexOrThrow("altura")),
                peso = cursor.getDouble(cursor.getColumnIndexOrThrow("peso")),
                distanciaPreferida = cursor.getDouble(cursor.getColumnIndexOrThrow("distancia_preferida")),
                usuarioSenha = cursor.getString(cursor.getColumnIndexOrThrow("usuario_senha"))
            )
        } else null
    }
}