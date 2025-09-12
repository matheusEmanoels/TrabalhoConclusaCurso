package br.edu.utfpr.trabalhoconclusaocurso.data.model

data class Atividade(
    val id: String,
    val idUsuario: String,
    val nome: String,
    val dataHora: String, // ISO-8601 "2025-09-03T14:30:00"
    val duracao: Long,     // em segundos
    val distancia: Double,
    val velocidadeMedia: Double,
    val caloriasPerdidas: Double?
)