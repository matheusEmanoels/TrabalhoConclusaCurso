package br.edu.utfpr.trabalhoconclusaocurso.data.model

data class Atividade(
    var id: String = "",
    var idUsuario: String = "",
    var nome: String = "",
    var dataHora: String = "",
    var duracao: Long = 0,
    var distancia: Double = 0.0,
    var velocidadeMedia: Double = 0.0,
    var caloriasPerdidas: Double? = 0.0
)