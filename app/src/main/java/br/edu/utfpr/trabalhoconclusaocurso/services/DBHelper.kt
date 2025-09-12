package br.edu.utfpr.trabalhoconclusaocurso.services

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Criação da tabela Usuario
        val createUsuario = """
            CREATE TABLE $TABLE_USUARIO (
                $COL_USUARIO_ID TEXT PRIMARY KEY,
                $COL_USUARIO_NOME TEXT NOT NULL,
                $COL_USUARIO_USERNAME TEXT NOT NULL UNIQUE,
                $COL_USUARIO_CPF TEXT NOT NULL,
                $COL_USUARIO_IDADE INTEGER NOT NULL,
                $COL_USUARIO_ALTURA REAL NOT NULL,
                $COL_USUARIO_PESO REAL NOT NULL,
                $COL_USUARIO_DISTANCIA_PREF REAL,
                $COL_USUARIO_SENHA TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createUsuario)

        // Criação da tabela Atividade
        val createAtividade = """
            CREATE TABLE $TABLE_ATIVIDADE (
                $COL_ATIVIDADE_ID TEXT PRIMARY KEY,
                $COL_ATIVIDADE_ID_USUARIO TEXT NOT NULL,
                $COL_ATIVIDADE_NOME TEXT NOT NULL,
                $COL_ATIVIDADE_DATAHORA TEXT NOT NULL,
                $COL_ATIVIDADE_DURACAO INTEGER NOT NULL,
                $COL_ATIVIDADE_DISTANCIA REAL NOT NULL,
                $COL_ATIVIDADE_VELOCIDADE_MEDIA REAL NOT NULL,
                $COL_ATIVIDADE_CALORIAS REAL,
                FOREIGN KEY($COL_ATIVIDADE_ID_USUARIO) REFERENCES $TABLE_USUARIO($COL_USUARIO_ID)
            )
        """.trimIndent()
        db.execSQL(createAtividade)

        // Criação da tabela Coordenada
        val createCoordenada = """
            CREATE TABLE $TABLE_COORDENADA (
                $COL_COORDENADA_ID TEXT PRIMARY KEY,
                $COL_COORDENADA_ID_ATIVIDADE TEXT NOT NULL,
                $COL_COORDENADA_LAT REAL NOT NULL,
                $COL_COORDENADA_LON REAL NOT NULL,
                FOREIGN KEY($COL_COORDENADA_ID_ATIVIDADE) REFERENCES $TABLE_ATIVIDADE($COL_ATIVIDADE_ID)
            )
        """.trimIndent()
        db.execSQL(createCoordenada)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COORDENADA")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ATIVIDADE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIO")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "app_corrida.db"
        const val DATABASE_VERSION = 3

        // Tabela Usuario
        const val TABLE_USUARIO = "Usuario"
        const val COL_USUARIO_ID = "id"
        const val COL_USUARIO_NOME = "nome"
        const val COL_USUARIO_USERNAME = "nome_usuario"
        const val COL_USUARIO_CPF = "cpf"
        const val COL_USUARIO_IDADE = "idade"
        const val COL_USUARIO_ALTURA = "altura"
        const val COL_USUARIO_PESO = "peso"
        const val COL_USUARIO_DISTANCIA_PREF = "distancia_preferida"
        const val COL_USUARIO_SENHA = "usuario_senha"

        // Tabela Atividade
        const val TABLE_ATIVIDADE = "Atividade"
        const val COL_ATIVIDADE_ID = "id"
        const val COL_ATIVIDADE_ID_USUARIO = "id_usuario"
        const val COL_ATIVIDADE_NOME = "nome"
        const val COL_ATIVIDADE_DATAHORA = "data_hora"
        const val COL_ATIVIDADE_DURACAO = "duracao"
        const val COL_ATIVIDADE_DISTANCIA = "distancia"
        const val COL_ATIVIDADE_VELOCIDADE_MEDIA = "velocidade_media"
        const val COL_ATIVIDADE_CALORIAS = "calorias_perdidas"

        // Tabela Coordenada
        const val TABLE_COORDENADA = "Coordenada"
        const val COL_COORDENADA_ID = "id"
        const val COL_COORDENADA_ID_ATIVIDADE = "id_atividade"
        const val COL_COORDENADA_LAT = "latitude"
        const val COL_COORDENADA_LON = "longitude"
    }
}