package br.edu.utfpr.trabalhoconclusaocurso.data.model

data class Atividade(
    val id: String,
    val idUsuario: String,
    val nome: String,
    val dataHora: String,
    val duracao: Long,
    val distancia: Double,
    val velocidadeMedia: Double,
    val caloriasPerdidas: Double?
)