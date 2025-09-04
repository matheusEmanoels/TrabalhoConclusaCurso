package br.edu.utfpr.trabalhoconclusaocurso.data.model

data class Usuario(
    val id: String,
    val nome: String,
    val cpf: String,
    val idade: Int,
    val altura: Double,
    val peso: Double,
    val distanciaPreferida: Double?,
    val usuarioSenha: String
)