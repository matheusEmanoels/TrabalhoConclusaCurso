package br.edu.utfpr.trabalhoconclusaocurso.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import br.edu.utfpr.trabalhoconclusaocurso.data.model.Usuario
import br.edu.utfpr.trabalhoconclusaocurso.services.DBHelper

class UsuarioDao(private val db: SQLiteDatabase) {

    fun inserir(usuario: Usuario): Long {
        val values = ContentValues().apply {
            put("id", usuario.id)
            put("nome", usuario.nome)
            put("nome_usuario", usuario.username)
            put("cpf", usuario.cpf)
            put("idade", usuario.idade)
            put("altura", usuario.altura)
            put("peso", usuario.peso)
            put("distancia_preferida", usuario.distanciaPreferida)
            put("usuario_senha", usuario.usuarioSenha)
        }
        return db.insert("Usuario", null, values)
    }

    fun atualizar(usuario: Usuario): Int {
        val values = ContentValues().apply {
            put(DBHelper.COL_USUARIO_NOME, usuario.nome)
            put(DBHelper.COL_USUARIO_USERNAME, usuario.username)
            put(DBHelper.COL_USUARIO_SENHA, usuario.usuarioSenha)
            put(DBHelper.COL_USUARIO_IDADE, usuario.idade)
            put(DBHelper.COL_USUARIO_ALTURA, usuario.altura)
            put(DBHelper.COL_USUARIO_PESO, usuario.peso)
            put(DBHelper.COL_ATIVIDADE_DISTANCIA, usuario.distanciaPreferida)
        }
        return db.update(
            DBHelper.TABLE_USUARIO,
            values,
            "${DBHelper.COL_USUARIO_ID} = ?",
            arrayOf(usuario.id)
        )
    }

    // Busca usuário por ID
    fun buscarPorId(id: String): Usuario? {
        val cursor = db.query(
            DBHelper.TABLE_USUARIO,
            null,
            "${DBHelper.COL_USUARIO_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        return cursor.use { if (it.moveToFirst()) toUsuario(it) else null }
    }

    // Busca usuário pelo username
    fun buscarPorUsername(username: String): Usuario? {
        val cursor = db.query(
            DBHelper.TABLE_USUARIO,
            null,
            "${DBHelper.COL_USUARIO_USERNAME}= ?",
            arrayOf(username),
            null,
            null,
            null
        )
        return cursor.use { if (it.moveToFirst()) toUsuario(it) else null }
    }

    // Lista todos os usuários
    fun listarTodos(): List<Usuario> {
        val cursor = db.query(
            DBHelper.TABLE_USUARIO,
            null,
            null,
            null,
            null,
            null,
            null
        )
        val usuarios = mutableListOf<Usuario>()
        cursor.use {
            while (it.moveToNext()) {
                usuarios.add(toUsuario(it))
            }
        }
        return usuarios
    }

    // Converte cursor para objeto Usuario
    private fun toUsuario(cursor: Cursor): Usuario {
        return Usuario(
            id = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_ID)),
            nome = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_NOME)),
            cpf = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_CPF)),
            username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_USERNAME)),
            usuarioSenha = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_SENHA)),
            idade = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_IDADE)),
            altura = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_ALTURA)),
            peso = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_PESO)),
            distanciaPreferida = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_DISTANCIA_PREF))
        )
    }
}